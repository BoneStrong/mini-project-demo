### quartz的不足
Quartz是一个流行的Java应用开源作业调度库

使用过程中有如下不足
1. 调用API的的方式操作任务，不人性化；
2. 需要持久化业务QuartzJobBean到底层数据表中，系统侵入性相当严重。
3. 调度逻辑和QuartzJobBean耦合在同一个项目中，这将导致一个问题，在调度任务数量逐渐增多，
   同时调度任务逻辑逐渐加重的情况下，此时调度系统的性能将大大受限于业务；
4. quartz底层以`抢占式`获取DB锁并由**抢占成功节点负责运行任务**，会导致节点负载悬殊非常大；

1-3问题点严格意义上讲其实不算不足。
api的调用多了一个中间层，其实是规范了使用，方便了扩展。
而QuartzJobBean的问题其实和api一样，同样可以把他只当成job的接口层使用，抽取成中间层，异步触发任务，而不是耦合业务使用。

最大的问题是第4点，quartz集群底层使用数据库锁来处理触发器。

Quartz提供的锁表qrtz_lock，为多个节点调度提供分布式锁，实现分布式调度，默认有2个锁：
- **STATE_ACCESS**主要用在scheduler定期检查是否失效的时候，保证只有一个节点去处理已经失效的scheduler；
- **TRIGGER_ACCESS**主要用在TRIGGER被调度的时候，保证只有一个节点去执行调度；

### quartz使用过程中的优化
Quartz在集群环境下使用数据库锁。常规配置的作业会在在高负载下堆叠。
所以使用**优化的核心其实是减少数据库的访问和锁争用**。

批量模式可以改善性能，减少锁次数也会有所帮助。
- 批量拉取的trigger数量建议和配置的线程次核心线程数一致
- 减少锁次数的方向是：尽量保持相关的任务在同一个线程执行，
  获取一次锁的过程尽量做好相关任务，减少线程的切换和数据库多次访问


### quartz改造优化思路
#### 改造前置知识：
quartz trigger在数据库的状态：
- WAITING 等待触发
- ACQUIRED 即将触发
- EXECUTING 执行
- COMPLETE 完成
- BLOCKED 阻塞
- ERROR 错误
- PAUSED 暂定
- PAUSED_BLOCKED 阻塞
- DELETED 删除

#### rendezvous
Hrw hash也是一个非常简洁高效的一致性hash算法，很好的解决了经典hash的数据均衡性和jump hash的单调性问题
```java
public int hrw(String key) {
        int maxHash = Integer.MIN_VALUE;
        String[] nodeIds = getActiveNodes(); //获取所有节点
        for (String nodeId : nodeIds) {
            if (hash(nodeId + key) > maxHash) { //计算key+nodeid的hash值
                maxHash = hash(nodeId + key);  //获取最大的hash
            }
        }
        return maxHash;
    }
```

改造的目的：
1. 多个quartz实例可以并行拉取trigger
2. 拉取trigger时无锁化

大致组件：
- **注册中心** 维持quartz实例心跳
- **quartz管理中心** （简化版可并入quartz实例）
  - 获取WAITING状态的triggers，负责对trigger分发，默认规则是根据约会hash指定执行的quartz实例。
    这样可以规避quartz节点故障或是扩容时没有节点及时执行trigger的问题。
    获取后quartz将相关的trigger状态设置为acquired。通过rpc请求发送trigger信息
- **quartz实例**
  
    获取trigger，判断是否是正常调度线程执行还是漏调度线程执行。
    - 正常调度线程 将trigger根据下次执行时间不同丢进不同的延迟消息队列。（中间表异步发送保证消息一定发送成功）
    - 漏调度
    
问题点：
1. 如何保证trigger不被重复触发？
   重复触发可能发生的场景：
   - quartz A,B两个实例并发拉取，trigger1 被A拉取，经过hrw hash判定可以被执行，
    则丢入fire trigger表，这样可以防止重复入库
     
   - trigger消息重复消费触发，这里主要是trigger消息被重复发送。
    接受trigger消息时先将消息丢入fireTrigger表，消费成功后记录最后执行时间，更新trigger状态
  
    

