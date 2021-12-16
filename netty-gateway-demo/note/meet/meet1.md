JVM

* JVM 内存模型
* 堆是一个什么样的结构
* CMS 垃圾回收的过程
* CMS 垃圾回收过程中会有几次 STW 的操作
* 根集包括哪些对象，为什么这些对象会作为根集对象

* 类加载机制、双亲委派模型

* 为什么要引入双亲委派机制 ：**防止出现多份相同的字节码，类加载器和类确定唯一性**
* JVM 内存区域分布？GC 发生在哪些部分？
* 介绍一下垃圾回收过程
* 垃圾回收算法了解多少？现在用的什么回收算法？
* 现在使用的什么垃圾回收器？知道哪些？讲讲 G1

* 容器的内存和 JVM 的内存有什么关系？参数怎么配置？

  Docker是通过linux-cgroup来实现最大内存设置的。而在新版本的Java 中对容器化做了相应的优化处理，
  **JVM通过感知linux-cgroup，从而获取Docker内的资源限制**
  新版本中Java 可以通过以下参数进行设置： -XX:+UseContainerSupport 开启是否感知容器(Docker)内存，在新版本的Java 中这一参数默认为True-开启。 -XX:
  MaxRAMPercentage进行设置JVM的最大堆内存比例，新版JVM中这一值默认为25.0。 例如：当Docker的最大内存为1000Mi时，JVM的最大**堆内存*为250Mi。

* 线上有什么 JVM 参数调整？
* oom 问题排查思路
* 线上问题排查，突然长时间未响应，怎么排查，oom
* cpu 使用率特别高，怎么排查？通用方法？定位代码？cpu 高的原因？
* 频繁 GC 原因？什么时候触发 FGC？
* 怎么获取 dump 文件？怎么分析？
* 怎么实现自己的类加载器？
* 类加载过程？
* 初始化顺序？
* Java 里边常见的垃圾回收器和垃圾回收算法
* CMS 垃圾回收器跟 G1 垃圾回收器的区别
* 对比 CMS，G1 垃圾回收器的优点有哪些

### 多线程并发

* 解释 Java 中的锁升级
* 解释可重入锁的概念及原理
* 解释自旋锁的概念及原理
* voliate关键字的使用场景及作用

* 进程间如何通信：进程 A 想读取进程 B 的主存怎么办？线程间通信？
    - 管道
    - 信号量
    - 套接字
    - 共享内存 共享内存就是映射一段能被其他进程所访问的内存，这段共享内存由一个进程创建，但多个进程都可以访问。 共享内存是最快的 IPC 方式，它是针对其他进程间通信方式运行效率低而专门设计的。
      它往往与其他通信机制，如信号两，配合使用，来实现进程间的同步和通信。

    - 消息队列  
      消息队列是由消息的链表，存放在内核中并由消息队列标识符标识。消息队列克服了信号传递信息少、 管道只能承载无格式字节流以及缓冲区大小受限等缺点。

* 线程的生命周期有哪些状态？怎么转换？
    - **NEW**:  Thread state for a thread which has not yet started
    - **RUNNABLE**:

    - **BLOCKED**: is waiting for a monitor lock to enter a synchronized block/method or reenter a synchronized
      block/method after calling Object.wait.

    - WAITING
    - **TIMED_WAITING**
        - Thread.sleep
        - Object.wait with timeout
        - Thread.join with timeout
        - LockSupport.parkNanos
        - LockSupport.parkUntil

    - **TERMINATED**：Thread state for a terminated thread. The thread has completed execution

* wait 和 sleep 有什么区别？什么情况下会用到 sleep？ wait ()和sleep ()的关键的区别在于，wait ()是用于线程间通信的，而sleep ()是用于短时间暂停当前线程。

* await 和 synchronized
    - 执行Object.wait()会使当前线程进入阻塞状态，并且释放占用的锁。
    - 执行Object.signal()会唤醒一个阻塞线程，使其进入就绪状态。此时被唤醒的线程会试图去获取锁。
    - **所以执行 wait和notify前需要在sync代码块里先获取锁**

* 怎么停止线程？
    - 标识位
    - stop() 不推荐，直接结束跳过了final,释放所有锁，数据可能有问题
    - interrupt 添加中断标识位，线程会抛出异常

* 怎么控制多个线程按序执行？
    - join
    - wait notify
    - CountDownLatch CyclicBarrier
    - Semaphore release
    - aqs
    - cas

* 会用到线程池么？怎么使用的？用什么实现的？
* 常用的线程池有哪些？用的哪个线程池？什么情况下怎么选择？

* ThreadPoolExecutor 有什么参数？各有什么作用？拒绝策略?
* 一个任务从被提交到被执行，线程池做了哪些工作？
    - corePoolSize 表示线程池的核心线程数。当有任务提交到线程池时，如果线程池中的线程数小于corePoolSize,那么则直接创建新的线程来执行任务。
    - workQueue 任务队列，它是一个阻塞队列，用于存储来不及执行的任务的队列。当有任务提交到线程池的时候，如果线程池中的线程数大于等于corePoolSize，那么这个任务则会先被放到这个队列中，等待执行。
    - maximumPoolSize
      表示线程池支持的最大线程数量。当一个任务提交到线程池时，线程池中的线程数大于corePoolSize,并且workQueue已满，那么则会创建新的线程执行任务，但是线程数要小于等于maximumPoolSize。
    - keepAliveTime 非核心线程空闲时保持存活的时间。非核心线程即workQueue满了之后，再提交任务时创建的线程，因为这些线程不是核心线程，所以它空闲时间超过keepAliveTime后则会被回收。
    - unit 非核心线程空闲时保持存活的时间的单位
    - threadFactory 创建线程的工厂，可以在这里统一处理创建线程的属性
    - handler 拒绝策略，当线程池中的线程达到maximumPoolSize线程数后且workQueue已满的情况下，再向线程池提交任务则执行对应的拒绝策略
        - DiscardOldestPolicy 当提交任务到线程池中被拒绝时，线程池会**丢弃等待队列中最老的任务**。
        - CallerRunsPolicy 当提交任务到线程池中被拒绝时，会在线程池当前正在运行的Thread线程中处理被拒绝额任务。即哪个线程提交的任务哪个线程去执行。
        - AbortPolicy 当提交任务到线程池中被拒绝时，直接抛出RejectedExecutionException异常。

* 了解 AQS 么？讲讲底层实现原理

> AbstractQueuedSynchronizer会把所有的请求线程构成一个CLH队列，当一个线程执行完毕（lock.unlock()）时会激活自己的后继节点，
> 但正在执行的线程并不在队列中，而那些等待执行的线程全部处于阻塞状态. 线程的显式阻塞是通过调用LockSupport.park()完成，
> 而LockSupport.park()则调用sun.misc.Unsafe.park()本地方法，再进一步，
> HotSpot在Linux中中通过调用pthread_mutex_lock函数把线程交给系统内核进行阻塞。该队列如图：

* AQS 有那些实现？
    - Semaphore
    - CountDownLatch
    - ReentrantLock
    - CyclicBarrier
    - ReentrantReadWriteLock
    - ThreadPoolExcutor

* 讲讲 AtomicInteger 的底层实现

  CAS,V表示要更新的变量，E表示预期值，N表示新值。仅当V值等于E值时，才会将V的值设为N。如果V值和E值不同，说明已经有了其他线程做了修改，则当前线程什么都不做

* volatile 关键字有什么用？怎么理解可见性，一般什么场景去用可见性

  什么是可见性 在并发编程中，线程安全问题的本质其实就是 原子性、有序性、可见性； 接下来主要围绕这三个问题进行展开分析其本质，彻底了解可见性的特性
    - 原子性 和数据库事务中的原子性一样，满足原子性特性的操作是不可中断的，要么全部执行成功要么全部执行失败
    - 有序性

      编译器和处理器为了优化程序性能而对指令序列进行重排序， 也就是你编写的代码顺序和最终执行的指令顺序是不一致的，重排序可能会导致多线程程序出现内存可见性问题

    - 可见性 多个线程访问同一个共享变量时，其中一个线程对这个共享变量值的修改，其他线程能够立刻获得修改以后的值

  为了彻底了解这三个特性，我们从两个层面来分析，第一个层面是硬件层面、第二个层面是JMM层面
  **导致可见性问题，本质是并发修改访问（重排序本质也是并发修改访问）导致**
  **CPU高速缓存以及指令重排序都会造成可见性问题**，解决方案如下
    - CPU层面的内存屏障
    - jvm的内存屏障
  > Java的volatile操作，在JVM实现层面第一步是给予了C++的原语实现。
  > c/c++中的volatile关键字，用来修饰变量，通常用于语言级别的 memory barrier。
  > 被volatile声明的变量表示随时可能发生变化，每次使用时，
  > 都必须从变量i对应的内存地址读取，编译器对操作该变量的代码不再进行优化

* 讲一下 threadLocal 原理，threadLocal 是存在 JVM 内存哪一块的。

  threadLocal是对象，存在java堆里面，要不然他弱引用如何回收？

  ThreadLocalMap使用ThreadLocal的弱引用作为key，如果一个ThreadLocal没有外部强引用来引用它， 那么系统 GC
  的时候，这个ThreadLocal势必会被回收，这样一来，ThreadLocalMap中就会出现key为null的Entry， 就没有办法访问这些key为null的Entry的value，如果当前线程再迟迟不结束的话，
  **这些key为null的Entry的value就会一直存在一条强引用链**： Thread Ref -> Thread -> ThreaLocalMap -> Entry -> value永远无法回收，造成内存泄漏。

  其实，ThreadLocalMap的设计中已经考虑到这种情况，也加上了一些防护措施： 在ThreadLocal的get(),set(),remove()的时候都会清除线程ThreadLocalMap里所有key为null的value。

  但是这些被动的预防措施并不能保证不会内存泄漏：

    - 使用static的ThreadLocal，延长了ThreadLocal的生命周期，可能导致的内存泄漏（参考ThreadLocal 内存泄露的实例分析）。
    - 分配使用了ThreadLocal又不再调用get(),set(),remove()方法，那么就会导致内存泄漏。

* 讲一下锁，有哪些锁，有什么区别，怎么实现的？

* ReentrantLock 应用场景

* 死锁条件

* 对线程安全的理解
  **当多个线程访问一个对象时，如果不进行额外的同步控制或者其他的协调操作， 调用这个对象的行为都可以获得正确的结果，我们就说这个对象是线程安全的**

* 乐观锁和悲观锁的区别？ 本质是一个在操作前检测锁（悲观，适合写多），一个在操作中检测锁（乐观，适合读多）

* 这两种锁在 Java和MySQL分别是怎么实现的？

* 事务有哪些特性？

* 怎么理解原子性？ 不能分割成更小的操作，要么成功，要么失败

* HashMap 为什么不是线程安全的？
* 怎么让HashMap变得线程安全？
* jdk1.8对ConcurrentHashMap做了哪些优化？

* Redis主从机制了解么？怎么实现的？
    - 增量同步aof,全量同步rdf
    - 同步依靠offset和backlog
    - 同步过程是异步的
    - 每个Redis服务器都会有一个表明自己身份的ID,这就是run id . 在主从复制时候如果根据host+ip定位master node，是不靠谱的，如果master node重启或者数据出现了变化， 那么slave
      node应该根据不同的run id区分， run id不同就做全量复制
    - Redis采用了乐观复制的策略，也就是在一定程度内容忍主从数据库的内容不一致

* 有过GC调优的经历么？

### Java 基础

* 了解 NIO 么？讲讲
* NIO 与 BIO 有什么区别？
* 了解 Netty 原理么
* Collection 有什么子接口、有哪些具体的实现
* 简单介绍下 ArrayList 怎么实现，添加操作、取值操作，什么时候扩容 ？

* 讲一下 hashMap 原理。hashMap 可以并发读么？并发写会有什么问题？ 可以并发读，不能并发写

* 讲一下 concurrentHashMap 原理。头插法还是尾插法？扩容怎么做？ hashmap 1.7头插法（多线程100%cpu问题），1.8尾插
  **concurrentHashMap是多线程进行扩容，采用头插法**

* 堆是怎么存储的，插入是在哪里？ 一般是用数组，插入到尾部，进行上浮

* 集合在迭代的过程中，插入或删除数据会怎样？ 如果是 for 的话
* int float short double long char 占字节数？
* int 范围？float 范围？

* hashcode 和 equals 的关系 默认是对象值和对象地址

* 深拷贝、浅拷贝区别 深拷贝和浅拷贝是只针对Object和Array这样的引用数据类型的 。 浅拷贝只复制指向某个对象的指针，而不复制对象本身，新旧对象还是共享同一块内存。 但深拷贝会另外创造一个一模一样的对象

* Java 异常体系？RuntimeException Exception Error 的区别，举常见的例子

* lambda 表达式中使用外部变量，为什么要 final？
* Java中的 HashMap、TreeMap 解释下？ treeMap 是红黑树实现的

* TreeMap 查询写入的时间复杂度多少？ logn
* ConcurrentHashMap 怎么实现线程安全的？
* HashMap 多线程有什么问题？怎么解决？
* CAS 和 synchronized 有什么区别？都用 synchronized 不行么？
* 为什么重写 equals 方法时，要求必须重写hashCode方法？
* 遍历 ArrayList 时候删除数据会发生什么 报并发修改异常

* ArrayList 扩容机制 注意：扩容并不是严格的1.5倍，是**扩容前的数组长度右移一位 + 扩容前的数组长度**

* Java 定时任务实现原理：优先队列
* Java 线程的几种状态
* 线程阻塞状态是什么意思
* 线程中断
* 当一个线程进入一个对象的一个 synchronized 方法后，其它线程是否可进入此对象的其它方法? 可以

### MySQL

* mysql 索引的数据结构，为什么用 B+ 树而不用 B 树

* 解释 mysql 的聚簇索引和非聚簇索引
* hash 索引
* Mysql 深分页怎么优化，不能分表分区
* mysql 里的聚簇索引和非聚簇索引，区别，使用主键索引和非主键索引查询时有什么区别
* mysql 里的事务隔离级别，具体含义，分别解决了什么问题
* 聚簇索引和聚簇促索引的区别
* mysql 的存储引擎
* innodb 和 myisam 的区别
* 为什么 myisam 不支持事务
* 为什么 myisam 不采用和 innodb 相同的方案来解决事务问题？
* 为什么数据量大的时候会出现慢 sql
* 慢 sql 如何解决 sql 优化
* 分库分表如何做的
* 分库分表如何做到不同库表间数据不重复
* 数据库水平切分，垂直切分的设计思路和切分顺序
* acid 含义？事务隔离级别？幻读怎么解决的？
* 用过 mysql 的锁么？有哪些锁？
* MyISAM、InnoDB 区别？为什么不用 MyISAM？
* mvcc 原理？多版本数据存放在哪？
* mysql 脏页？
* redo log，undo log？
* innodb 的索引结构是什么？什么是聚簇索引？
* b+ 树与 b 树的区别？
* b+ 树与二叉树区别，优点？为什么不用红黑树？
* 多列索引的结构
* 字符串类型和数字类型索引的效率？数据类型隐式转换
* 主键与普通索引的联系？存储上的区别？
* join 和 in 怎么选择？有什么区别？
* union 和 union all 有什么区别？怎么选择？
* 怎么处理 sql 慢查询？
* 索引用得不太正常怎么处理？同时有（a，b）和（a，c）的索引，查询 a 的时候，会选哪个索引？
* 跨库分页的实现？
* 分库分表有哪些策略？怎么保证 id 唯一？
* 对 uuid 的理解？知道哪些 GUID、Random 算法？
* 主键选随机 id、uuid 还是自增 id？为什么？主键有序无序对数据库的影响？
* 主从复制的过程？复制原理？怎么保证强一致性？

### Kafka

* Kafka 里边有几种基本角色，每个角色具体职责是什么
* Kafka 里怎么保证高可用性 分区副本
* Kafka 里的 rebalance 是怎么回事，怎么触发 消费者分区消费得再平衡 重平衡可以说是kafka为人诟病最多的一个点了。重平衡其实就是一个协议， 它规定了如何让消费者组下的所有消费者来分配topic中的每一个分区。
  比如一个topic有100个分区，一个消费者组内有20个消费者，在协调者的控制下让组内每一个消费者分配到5个分区， 这个分配的过程就是重平衡。

  重平衡的触发条件主要有三个：
    - **消费者组内成员发生变更**，这个变更包括了增加和减少消费者。注意这里的减少有很大的可能是被动的
    - **主题的分区数发生变更**: kafka目前只支持增加分区;当增加的时候就会触发重平衡
    - **订阅的主题发生变化** ;当消费者组使用正则表达式订阅主题;而恰好又新建了对应的主题
  
* 用 Kafka 做了什么功能？
* Kafka 内部原理？工作流程？
* Kafka 怎么保证数据可靠性？
  
  分区副本，ack,ISR,消费commit
  
* 怎么实现 Exactly-Once ？
  见文章[message_qos.md](../mq/message_qos.md)
* Kafka 选主怎么做的？ 见文章[mq_election](../mq/mq_election.md)
* Kafka 与 rabbitmq区别
* Kafka 分区怎么同步的
  ISR过程
* Kafka 怎么保证不丢消息的。
* Kafka 为什么可以扛住这么高的qps
  批量发送，批量拉取，顺序写顺序读，nio

### Redis

* Redis 的持久化储存有哪几种，各自的特点

  RDB 是一个非常紧凑（compact）的文件，它保存了 Redis 在某个时间点上的数据集。 这种文件非常适合用于进行备份： 比如说，你可以在最近的 24 小时内，每小时备份一次 RDB 文件，并且在每个月的每一天，也备份一个 RDB
  文件。 这样的话，即使遇上问题，也可以随时将数据集还原到不同的版本。
  **RDB 非常适用于灾难恢复（disaster recovery）**：它只有一个文件，并且内容都非常紧凑，可以（在加密后）将它传送到别的数据中心， 或者亚马逊 S3 中。RDB 可以最大化 Redis 的性能：父进程在保存 RDB
  文件时唯一要做的就是 fork 出一个子进程， 然后这个子进程就会处理接下来的所有保存工作，父进程无须执行任何磁盘 I/O 操作。**RDB 在恢复大数据集时的速度比 AOF 的恢复速度要快**。

RDB 的缺点:
如果你需要尽量避免在服务器故障时丢失数据，那么 RDB 不适合你。 虽然 Redis 允许你设置不同的保存点（save point）来控制保存 RDB 文件的频率， 但是， 因为RDB 文件需要保存整个数据集的状态，
所以它并不是一个轻松的操作。 因此你可能会至少 5 分钟才保存一次 RDB 文件。 在这种情况下， 一旦发生故障停机， 你就可能会丢失好几分钟的数据。 每次保存 RDB 的时候，Redis 都要 fork()
出一个子进程，并由子进程来进行实际的持久化工作。 在数据集比较庞大时， fork() 可能会非常耗时，造成服务器在某某毫秒内停止处理客户端； 如果数据集非常巨大，并且 CPU 时间非常紧张的话，那么这种停止时间甚至可能会长达整整一秒。 虽然
AOF 重写也需要进行 fork() ，但无论 AOF 重写的执行间隔有多长，数据的耐久性都不会有任何损失。

AOF 的优点:
使用 AOF 持久化会让 Redis 变得非常耐久（much more durable）： 你可以设置不同的 fsync 策略，比如无 fsync ，每秒钟一次 fsync ，或者每次执行写入命令时 fsync 。 AOF
的默认策略为每秒钟 fsync 一次，在这种配置下，Redis 仍然可以保持良好的性能，并且就算发生故障停机， 也最多只会丢失一秒钟的数据（ fsync 会在后台线程执行，所以主线程可以继续努力地处理命令请求）。 AOF
文件是一个只进行追加操作的日志文件（append only log）， 因此对 AOF 文件的写入不需要进行 seek ， 即使日志因为某些原因而包含了未写入完整的命令（比如写入时磁盘已满，写入中途停机，等等），
redis-check-aof 工具也可以轻易地修复这种问题。 Redis 可以在 AOF 文件体积变得过大时，自动地在后台对 AOF 进行重写： 重写后的新 AOF 文件包含了恢复当前数据集所需的最小命令集合。
整个重写操作是绝对安全的，因为 Redis 在创建新 AOF 文件的过程中，会继续将命令追加到现有的 AOF 文件里面， 即使重写过程中发生停机，现有的 AOF 文件也不会丢失。 而一旦新 AOF 文件创建完毕，Redis 就会从旧 AOF
文件切换到新 AOF 文件，并开始对新 AOF 文件进行追加操作。 AOF 文件有序地保存了对数据库执行的所有写入操作， 这些写入操作以 Redis 协议的格式保存， 因此 AOF 文件的内容非常容易被人读懂，
对文件进行分析（parse）也很轻松。 导出（export） AOF 文件也非常简单： 举个例子， 如果你不小心执行了 FLUSHALL 命令， 但只要 AOF 文件未被重写， 那么只要停止服务器， 移除 AOF 文件末尾的
FLUSHALL 命令， 并重启 Redis ， 就可以将数据集恢复到 FLUSHALL 执行之前的状态。

AOF 的缺点:
对于相同的数据集来说，AOF 文件的体积通常要大于 RDB 文件的体积。 根据所使用的 fsync 策略，AOF 的速度可能会慢于 RDB 。 在一般情况下， 每秒 fsync 的性能依然非常高， 而关闭 fsync 可以让 AOF
的速度和 RDB 一样快， 即使在高负荷之下也是如此。 不过在处理巨大的写入载入时，RDB 可以提供更有保证的最大延迟时间（latency）。 AOF 在过去曾经发生过这样的 bug ： 因为个别命令的原因，导致 AOF
文件在重新载入时，无法将数据集恢复成保存时的原样。 （举个例子，阻塞命令 BRPOPLPUSH 就曾经引起过这样的 bug 。） 测试套件里为这种情况添加了测试： 它们会自动生成随机的、复杂的数据集， 并通过重新载入这些数据来确保一切正常。
虽然这种 bug 在 AOF 文件中并不常见， 但是对比来说， RDB 几乎是不可能出现这种 bug 的。

* 如果 Redis 崩溃，rdb 和 aof 选用那种进行恢复，为什么

  般来说,如果想达到足以媲美 PostgreSQL 的数据安全性， 你应该同时使用两种持久化功能。 如果你非常关心你的数据,但仍然可以承受数分钟以内的数据丢失， 那么你可以只使用 RDB 持久化。 有很多用户都只使用 AOF 持久化，
  但我们并不推荐这种方式： 因为定时生成 RDB 快照（snapshot）非常便于进行数据库备份， 并且 RDB 恢复数据集的速度也要比 AOF 恢复的速度要快， 除此之外， 使用 RDB 还可以避免之前提到的 AOF 程序的 bug 。
  因为以上提到的种种原因， 未来我们可能会将 AOF 和 RDB 整合成单个持久化模型。 （这是一个长期计划。）

  Redis 还可以同时使用 AOF 持久化和 RDB 持久化。 在这种情况下， 当 Redis 重启时， 它会优先使用 AOF 文件来还原数据集， 因为 AOF 文件保存的数据集通常比 RDB
  文件所保存的数据集更完整。你甚至可以关闭持久化功能，让数据只在服务器运行时存在。


* Redis 怎么做分布式锁，有几种方法，Redis 作为分布式锁有什么优势 setNx,需要额外编写lua脚本保障和设置时间是一个原子操作

  第三方框架：redisson分布式锁，底层也是setnx+lua,但是额外实现重入 Redisson可以实现可重入加锁机制的原因，我觉得跟两点有关：
    - 1、Redis存储锁的数据类型是 Hash类型
    - 2、Hash数据类型的key值包含了当前线程信息。

* Redis 里的事务，与 mysql 事务的区别 redis事务其实相当于一个命令集合，不支持回滚，严格来说不是事务

* Redis 的 ziplist 数据结构，越详细越好
* Redis 复制的过程越详细越好 Redis提供了非常简单的命令：slaveof。我们只需要在节点B上执行以下命令，就可以让节点B成为节点A的数据副本

* Redis 常用数据结构
* zset 的数据结构
* zset 中插入一个元素的时间复杂度
* Redis 如何保证高可用？
* 什么是 Redis 的哨兵模式？
* Redis 主备切换过程是怎样的？
* Redis 数据结构、对象，使用场景
* 缓存的热点 Key 怎么处理？Redis 缓存穿透，怎么避免？
* Redis keys 命令有什么缺点
* 主从同步原理，新加从库的过程
* RDB 和 AOF 怎么选择，什么场景使用？
* Redis 的 zset 的使用场景？底层实现？为什么要用跳表？
* 怎么实现 Redis 分布式锁？
* Redis 4.0 有什么新特性？
* Redis 的过期时间
* Redis 的 lru 策略： 随机、Key最近被访问的时间 、Key的过期时间(TTL)

### Spring

* 讲一下 IOC、AOP
* ioc 怎么防止循环依赖
* aop 的实现原理、动态代理过程
* spring boot starter 自加载是怎么实现的？在生命周期哪个阶段？
* Spring 处理请求的过程？
* SpringMVC 不同用户登录的信息怎么保证线程安全的？

### 操作系统

* select，poll，epoll，epoll 的优势
* 进程和线程的区别，linux 切换进程线程时具体做了哪些操作，越详细越好
* 协程了不了解，为什么轻量级，对比进程线程讲一讲

  算法题

* 【算法】一棵树，求最大同源路径
* 【算法】数组中右边第一个比他大元素（时间复杂度O（N））
* 【算法】快速排序 o(nlogn)
* 【算法】LRU 缓存变形题（带有过期时间的lru缓存）
* 【算法】栈的所有可能弹出序列
* 【算法】三数之和，如果是四数之和如何做
* 【设计题】微信抢红包架构设计
* 【算法】二分法