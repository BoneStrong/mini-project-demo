### 任务分配优化
问题：
- 任务分片
  - 简单分片：task需要增加字段，分片规则，分片数量（需要业务指定），分片编号
  - 自动分片：分片规则（如资源优先，按照工作组的work节点资源比例动态分片;
    如权重优先，则根据work节点权重进行分片），分片数量
  
- 负载均衡
  master这边是简单的单线程轮询DAG的任务链，依次执行DAG的task，线程等待时间较长，浪费cpu资源
  work节点也是无脑抓取批量任务多线程执行，分布式锁限制任务并行抓取，无脑抓取也导致系统资源使用不均衡

为提高master和work吞吐量，可以引入消息队列。

比如任务链 s->a，a->b,a->c,b->d,c->d,a的执行只依赖s,但d的执行依赖b和c的结果。

DAG优化执行方案如下：
1. master解析DAG1，将s,a,b,c,d 生成taska,b,c,d定义入库(task a定义包括前置依赖s,后续c、d，所属processDef),发送DagEvent(Dag1) 
2. master收到DagEvent(Dag1),开始执行Dag,分析生成首个task s。
   判断task a是否需要分片：
   - 需要分片
     根据分片信息（分片数量，分片规则）生成子task， 
     对task执行进行work节点负载均衡（采用工作组_加编号N形式，不采用Ip是为了work节点的扩展性），标记执行节点编号 
     子task记录task及分片信息,持久化入库，标记开始执行状态，并**发送TaskExeEvent(task_s_N,N{1...N})到工作组广播队列**
   - 不需要分片 
     直接生成TaskS, 并**发送TaskExeEvent(task_s)到工作组订阅队列**
    

3. work监听广播队列和订阅队列的TaskExeEvent消息，分两种模式执行。
   - task是分片任务，则是监听广播队列，监听TaskExeEvent(task_a_N),判断编号是否和自己匹配，匹配则进行执行
   - task是正常任务，则是监听的订阅队列，任务直接执行
     
    执行完s后修改Task s状态持久化入库，标记执行机器信息，并发送TaskCompleteEvent(s->a)，
   
4. master 监听到TaskCompleteEvent(s->a),可以判断a的执行条件是否达成（数据库检测s的状态是否完成，a是否有其他前置条件）
   如果a执行条件达成，则发送TaskPreEnQueueEvent(a)至消息队列。
   
5. master监听TaskPreEnQueueEvent(a),同步骤2处理。任务推送队列 TaskExeEvent(task_a_N)
6. work监听TaskExeEvent(task_a_N),判断编号是否和自己匹配，匹配则进行执行，
   work执行完task_a_N对a任务进行判断，是否分片均执行完毕？执行完毕则发送TaskCompleteEvent(a->b,a->c)
7. b,c执行循环2-6，最后完成是发送TaskCompleteEvent(b->d,c->d)
8. 无论TaskCompleteEvent(b->d,c->d)先后，消息都判断d的前置执行条件是否满足
     （前置条件是b,c的task状态均成功完成或者自定义条件）
   
9. 最后d执行完，task d定义带有Dag标识，发送DagCompletedEvent消息队列，master监听消息后完成处理

该方案适合任务量极多的业务场景，但缺陷是每个消息可能都带有一次数据库查询来确认任务状态，
针对这种对数据的压力，可以采用分库分表的策略，根据工作组维度进行分库分表。

### 任务执行优化
- 失败任务判定
  task定义超时时间，master定期轮询task Instance表查找运行中状态切超时时间小于当前时间的任务。
  找到则重新构建task_s_N消息，让其他work节点执行
- 失败任务重试
  每次执行会增加task Instance的执行次数，如果超过执行次数，则根据定义策略拒绝服务或者进行告警

