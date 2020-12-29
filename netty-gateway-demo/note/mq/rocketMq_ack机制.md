### 生产大致流程
### 消费大致流程
消费的获取拉消息模式：
- push方式：
    consumer把轮询过程封装了，并注册MessageListener监听器，取到消息后，
    唤醒MessageListener的consumeMessage()来消费，对用户而言，感觉消息是被推送过来的。

- pull方式：
    取消息的过程需要用户自己写，首先通过打算消费的Topic拿到MessageQueue的集合，
    遍历MessageQueue集合，然后针对每个MessageQueue批量取消息，一次取完后，记录该队列下一次要取的开始offset，直到取完了，再换另一个MessageQueue。

> 相当于
push的方式是：消息发送到broker后，如果是push，则broker会主动把消息推送给consumer即topic中。
而pull的方式是:消息投递到broker后，消费端需要主动去broker上拉消息，即需要手动写代码实现。
本质都是pull消息。

以官网代码 demo为例
```java
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("test-group");
        consumer.setNamesrvAddr("xxxx:9876");
        consumer.subscribe("test-topic", "*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {

            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> msgs,
                                                            ConsumeConcurrentlyContext context) {
                System.out.printf("%s Receive New Messages: %s %n", Thread.currentThread().getName(), msgs);
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        System.out.printf("Consumer Started.%n");
```
从代码可以大致看到消费过程，这里是push方式获取消息。
1. 设置mq broken地址，消费监听器，初始化DefaultMQPushConsumer
2. start()启动消费者

这里面基本逻辑在DefaultMQPushConsumerImpl.start()中，构造DefaultMQPushConsumerImpl的过程中，服务的的状态是ServiceState.CREATE_JUST。
可以在org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl#start的create_just分支看到主要是做了以下事情：
1. 整个方法是加锁的，跟PullConsumer的一样：
    先调用checkConfig()，确认consumer的配置是否合法，比如消费者组名，消息模式，是否顺序消费，消息队列分配策略等。
2. 然后调用copySubscription()方法，将DefaultPushConsumer的订阅信息构造成SubscriptionData复制到DefaultPushConsumerImpl的subscriptionInner中。
3. 然后是消费者客户端MQClientInstance实例的获取过程。接下来配置reblanceImpl、构造pullAPIWrapper实例并给其注册FilterMessageHook。
4. 然后根据消费者的消息模式，选择不同的方式存储消费进度，广播则本地文件，集群则存于远程broker服务器中。
    我们看下本地文件的方式即LocalFileOffsetStore的方式。

上述比较重要的部分是MqClientInstance实例的获取过程
```java
public MQClientInstance(ClientConfig clientConfig, int instanceIndex, String clientId, RPCHook rpcHook) {
        this.clientConfig = clientConfig;
        this.instanceIndex = instanceIndex;
        //netty客户端配置
        this.nettyClientConfig = new NettyClientConfig();
        this.nettyClientConfig.setClientCallbackExecutorThreads(clientConfig.getClientCallbackExecutorThreads());
        this.nettyClientConfig.setUseTLS(clientConfig.isUseTLS());
        this.clientRemotingProcessor = new ClientRemotingProcessor(this);
        //设置netty rpc请求HOOK
        this.mQClientAPIImpl = new MQClientAPIImpl(this.nettyClientConfig, this.clientRemotingProcessor, rpcHook, clientConfig);

        if (this.clientConfig.getNamesrvAddr() != null) {
            this.mQClientAPIImpl.updateNameServerAddressList(this.clientConfig.getNamesrvAddr());
            log.info("user specified name server address: {}", this.clientConfig.getNamesrvAddr());
        }
        this.clientId = clientId;
        //用来创建topic，获取队列，offset等信息
        this.mQAdminImpl = new MQAdminImpl(this);
        //主要拉取信息的实现，可以看到，本质就是pull的方式
        this.pullMessageService = new PullMessageService(this);

        this.rebalanceService = new RebalanceService(this);
        this.defaultMQProducer = new DefaultMQProducer(MixAll.CLIENT_INNER_PRODUCER_GROUP);
        this.defaultMQProducer.resetClientConfig(clientConfig);
        this.consumerStatsManager = new ConsumerStatsManager(this.scheduledExecutorService);
}
```

查看PullMessageService可以看到大致逻辑：
1. 其他线程将PullRequest放入pullRequestQueue中
2. PullMessageService线程轮询这个queue获取PullRequest
3. 通过获取的PullRequest拿到ProcessQueue，具体实现（org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl#pullMessage）
4. pullMessage过程中会进行检查和限流，设置回调函数pullCallback
5. this.pullAPIWrapper.pullKernelImpl()执行真正的拉取逻辑，最终通过MQClientAPIImpl拉取到消息
6. 拉取消息默认是异步的方式，拉取成功后执行上面的回调函数
7. 消息拉取成功后通过ConsumeMessageService构造成ConsumeRequest。ConsumeRequest是一个线程实例，实质跑的内容就是
    我们注册的监听器，如果是并发消费则是 MessageListenerConcurrently 实现类。
    最终执行listener.consumeMessage(Collections.unmodifiableList(msgs), context)

看下源码，解决了以前批量拉数据成功一半失败一半的疑惑
```java
public interface MessageListenerConcurrently extends MessageListener {
    /**
     * It is not recommend to throw exception,rather than returning ConsumeConcurrentlyStatus.RECONSUME_LATER if
     * consumption failure
     *
     * @param msgs msgs.size() >= 1<br> DefaultMQPushConsumer.consumeMessageBatchMaxSize=1,you can modify here
     * @return The consume status
     */
    ConsumeConcurrentlyStatus consumeMessage(final List<MessageExt> msgs,
        final ConsumeConcurrentlyContext context);
}
```

#### 处理消息结果
并发消费实现：org.apache.rocketmq.client.impl.consumer.ConsumeMessageConcurrentlyService#processConsumeResult，
org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl#sendMessageBack
消费成功者删除消息，更新偏移量。
消费失败重新发送给broken (延迟粒度和重试次数的设置)

#### 消费总结
1. DefaultMQPushConsumerImpl.start()是消费的入口
2. rebalanceService.start()作为独立线程生产pullRequest请求放入 pullRequestQueue
3. DefaultMQPushConsumerImpl 拉取消息后通过 ConsumeMessageService 构造成 ConsumeRequest
4. ConsumeRequest 作为线程实例跑我们注册的 MessageListenerConcurrently。

#### Rebalance简介
> Rebalance(再均衡)机制指的是：将一个Topic下的多个队列(或称之为分区)，
在同一个消费者组(consumer group)下的多个消费者实例(consumer instance)之间进行重新分配。 

 **Rebalance机制本意是为了提升消息的并行处理能力**。
 例如，一个Topic下5个队列，在只有1个消费者的情况下，那么这个消费者将负责处理这5个队列的消息。
 如果此时我们增加一个消费者，那么可以给其中一个消费者分配2个队列，给另一个分配3个队列，从而提升消息的并行处理能力。
 
但是Rebalance机制也存在明显的限制与危害。
 
- Rebalance限制：
 
 由于一个队列最多分配给一个消费者，因此当某个消费者组下的消费者实例数量大于队列的数量时，多余的消费者实例将分配不到任何队列。
 
- Rebalance危害： 
 
 除了以上限制，更加严重的是，在发生Rebalance时，存在着一些危害，如下所述：
 
 1. 消费暂停：考虑在只有Consumer 1的情况下，其负责消费所有5个队列；
    在新增Consumer 2，触发Rebalance时，需要分配2个队列给其消费。
    那么Consumer 1就需要停止这2个队列的消费，等到这两个队列分配给Consumer 2后，这两个队列才能继续被消费。
 2. 重复消费：Consumer 2 在消费分配给自己的2个队列时，必须接着从Consumer 1之前已经消费到的offset继续开始消费。
    然而默认情况下，offset是异步提交的，如consumer 1当前消费到offset为10，但是异步提交给broker的offset为8；
    那么如果consumer 2从8的offset开始消费，那么就会有2条消息重复。
    也就是说，Consumer 2 并不会等待Consumer1提交完offset后，再进行Rebalance，因此提交间隔越长，可能造成的重复消费就越多。 
 3. 消费突刺：由于rebalance可能导致重复消费，如果需要重复消费的消息过多；或者因为rebalance暂停时间过长，导致积压了部分消息。
    那么都有可能导致在rebalance结束之后瞬间可能需要消费很多消息。
    
基于Rebalance可能会给业务造成的负面影响，我们有必要对其内部原理进行深入剖析，以便于问题排查。我们将从Broker端和Consumer端两个角度来进行说明：
 
Broker端主要负责Rebalance元数据维护，以及通知机制，在整个消费者组Rebalance过程中扮演协调者的作用；
 
而Consumer端分析，主要聚焦于单个Consumer的Rebalance流程。
 
##### Broker端Rebalance协调机制
从本质上来说，触发Rebalance的根本因素无非是两个：
1. 订阅Topic的队列数量变化
2. 消费者组信息变化。
 
 
 #### 一些路由的问题
1. 如何将特定消息发送至特定queue，消费者从特定queue消费
- 生产者：
    > public Result<SendResult> send(Message msg, MessageQueueSelector selector, Object arg)
    自定义实现MessageQueueSelector
- 消费者：
    自定义实现一个AllocateMessageQueueStrategy进行queue的分配

其实两个端主要操作是选择MessageQueue


#### 长轮询
源码中有这一行设置语句requestHeader.setSuspendTimeoutMillis(brokerSuspendMaxTimeMillis)，
设置Broker最长阻塞时间，默认设置是15秒，注意是Broker在没有新消息的时候才阻塞，有消息会立刻返回。 

从Broker的源码中可以看出，服务端接到新消息请求后，如果队列里没有新消息，并不急于返回，通过一个循环不断查看状态，
每次 waitForRunning一段时候(默认是5秒)，然后后再Check。默认情况下当Broker一直没有新消息，第三次Check的时候，
等待时间超过Request里面的 BrokerSuspendMaxTimeMillis，就返回空结果。在等待的过程中，
Broker收到了新的消息后会直接调用notifyMessageArriving函数返回请求结果。

“长轮询”的核心是，Broker端HOLD住客户端过来的请求一小段时间，在这个时间内有新消息到达，就利用现有的连接立刻返回消息给Consumer。
“长轮询”的主动权还是掌握在Consumer手中，Broker即使有大量消息积压，也不会主动推送给Consumer。
长轮询方式的局限性，是在HOLD住Consumer请求的时候需要占用资源，它适合用在消息队列这种客户端连接数可控的场景中。

3.DefaultMQPullConsumer
使用DefaultMQPullConsumer像使用DefaultMQPushConsumer一样需要设置各种参数，写处理消息的函数，同时还需要做额外的事情。
接下来结合org.apache.rocketmq.example.simple包中的例子源码来介绍。
示例代码的处理逻辑是逐个读取某Topic下所有Message Queue的内容，读完一遍后退出，主要处理额外的三件事情：
(1) 获取Message Queue并遍历
一个Topic包括多个Message Queue，如果这个Consumer需要获取Topic下所有的消息，就要遍历多有的Message Queue。
如果有特殊情况，也可以选择某些特定的Message Queue来读取消息。
(2) 维护Offsetstore
从一个Message Queue里拉取消息的时候，要传入Offset参数(long类型的值)，随着不断读取消息，Offset会不断增长。
这个时候由用户负责把Offset存储下来，根据具体情况可以存到内存里、写到磁盘或者数据库里等。
(3) 根据不同的消息状态做不同的处理
拉取消息的请求发出后，会返回：FOUND，NO_MATCHED_MSG，NO_NEW_MSG，OFFSET_ILLEGAL四种状态，
要根据每个状态做不同的处理。比较重要的两个状态是FOUNT和NO_NEW_MSG，分别表示获取到消息和没有新的消息
实际情况中可以把while(true)放到外层，达到无限循环的目的。
因为PullConsumer需要用户自己处理遍历Message Queue、保存Offset，所以PullConsumer有更多的自主性和灵活性。