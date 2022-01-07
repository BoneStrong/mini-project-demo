### 基本组件

1. JobManager: 管理dataFlow的Task Graph的解析生成。Slot的申请，分配，和task的绑定。Task的提交
2. TaskManager: Task的执行
3. ResourceManger: Slot pool的统一管理

### 基本任务流程

问题：

1. jobGraph是如何生成的？
2. task是如何获取依赖关系？
3. 数据是在怎么流动分发的？

流程：

> 流计算的DAG转换3阶段： Transformation(source,sink,function ..) -> StreamGraph -> JobGraph(JobVertex) -> ExecutionGraph
> StreamGraph转换到JobGraph 中间可以进行一些优化，比如算子融合，避免了数据传输时的线程切换，网络开销
>
>

- 作业提交后，jobManager为每个作业生成一个JobMaster。JobMaster负责资源申请、调度、容错等
- **JobMaster将JobGraph转换成ExecutionGraph(可执行的图)**,接着SchedulerNG(默认调度器)负责将task部署到TaskManager.
- 以流计算调度为例，流计算需要获取Graph上所有的ExecuteVertex(对应算子的Task)及所需要的slot。最后调用Execution.Deploy()到TaskManager

### 计算流程

### 状态管理/一致性管理

checkpoint barrier 分布式轻量级快照

1. jobMaster 触发检查点。JobMaster在开始进行作业调度时，会调用CheckpointCoordinator进行检查点触发
2. taskManager 执行检查点
3. jobMaster 确认检查点

Flink 的 Checkpoint 有以下先决条件：

- 需要具有**持久性**且**支持重放一定时间范围内数据**的数据源。例如：Kafka、RabbitMQ 等。
- 需要一个能保存状态的持久化存储介质，例如：HDFS、S3 等。

Flink 中 Checkpoint 是默认关闭的，对于需要保障 At Least Once 和 Exactly Once 语义的任务， 强烈建议开启 Checkpoint，对于丢一小部分数据不敏感的任务，可以不开启 Checkpoint，
例如：一些推荐相关的任务丢一小部分数据并不会影响推荐效果。下面来介绍 Checkpoint 具体如何使用。

首先调用 StreamExecutionEnvironment 的方法 enableCheckpointing(n) 来开启 Checkpoint，参数 n 以毫秒为单位表示 Checkpoint 的时间间隔

#### checkPoint 开启配置
```text
// 开启 Checkpoint;每 1000毫秒进行一次 Checkpoint
env.enableCheckpointing(1000);

// Checkpoint 语义设置为 EXACTLY_ONCE
env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);

// CheckPoint 的超时时间
env.getCheckpointConfig().setCheckpointTimeout(60000);

// 同一时间;只允许 有 1 个 Checkpoint 在发生
env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);

// 两次 Checkpoint 之间的最小时间间隔为 500 毫秒
env.getCheckpointConfig().setMinPauseBetweenCheckpoints(500);

// 当 Flink 任务取消时;保留外部保存的 CheckPoint 信息
env.getCheckpointConfig().enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

// 当有较新的 Savepoint 时;作业也会从 Checkpoint 处恢复
env.getCheckpointConfig().setPreferCheckpointForRecovery(true);

// 作业最多允许 Checkpoint 失败 1 次;flink 1.9 开始支持;
env.getCheckpointConfig().setTolerableCheckpointFailureNumber(1);

// Checkpoint 失败后;整个 Flink 任务也会失败;flink 1.9 之前;
env.getCheckpointConfig.setFailTasksOnCheckpointingErrors(true)
```

**Checkpoint的流程**：

1. JobManager 端的 CheckPointCoordinator 会定期向所有 SourceTask 发送 CheckPointTrigger， Source Task 会在数据流中安插 Checkpoint barrier
2. 当 task 收到上游所有实例的 barrier 后，向自己的下游继续传递 barrier，然后自身同步进行快照， 并将自己的状态异步写入到持久化存储中(关于barrier会在后续flink进阶系列中再详细讲)
3. 当 task 将状态信息完成备份后，会将备份数据的地址（state handle）通知给 JobManager 的CheckPointCoordinator， 如果 Checkpoint 的持续时长超过了 Checkpoint
   设定的超时时间CheckPointCoordinator 还没有收集完所有的 State Handle， CheckPointCoordinator 就会认为本次 Checkpoint 失败，会把这次 Checkpoint
   产生的所有状态数据全部删除
4. 如果 CheckPointCoordinator 收集完所有算子的 State Handle，CheckPointCoordinator 会把整个 StateHandle 封装成 completed Checkpoint Meta，
   写入到外部存储中，Checkpoint 结束

**状态如何从 Checkpoint 恢复**： 如果是yarn提交任务，在常规任务基础上加上-s : hdfs://xxx/xxx/xxx/chk-10 chk-10就表示checkpoint做到第10次了，下次从此处恢复任务。

那么如何知道任务最后一次的checkpoint地址呢，可以通过调用api的方式：
http://node107.bigdata.dmp.local.com:35524/jobs/a1c70b36d19b3a9fc2713ba98cfc4a4f/metrics?get=lastCheckpointExternalPath
地址中端口号往前是Flink UI界面的地址，后面换成自己的jobId就行了，得到的结果就有checkPoint地址了

### 容错设计

#### exactly-once 的保证

问题：如果下级存储不支持事务，Flink 怎么保证 exactly-once？

解答：端到端的 exactly-once 对 sink 要求比较高，具体实现主要有**幂等写入** 和 **事务性写入**两种方式。 

幂等写入的场景依赖于业务逻辑，更常见的是用事务性写入。

而事务性写入又有**预写日志**（WAL)和 **两阶段提交**（2PC)两种方式。
> 如果外部系统不支持事务，那么可以用预写日志的方式，把结果数据先当成状态保存，然后在收到 checkpoint 完成的通知时，一次性写入 sink 系统。

#### 2pc
要求：
1. 数据源支持断点读取。比如kafka可以记录上次的offset,失败了从断点处继续读取。 **对source的要求**
2. 外部存储支持回滚或者保证幂等性。 **对sink的要求**
   - 回滚机制(即事务)： 作业失败后可以将部分写入结果回滚
   - 幂等性：失败后重新写入没有副作用