## ignite内存分配调优
#### 内存架构
多层架构是一种基于固定大小页面的内存架构，这些页面存储在内存（Java堆外）的托管非堆区中，并按磁盘上的特定层次结构进行组织。

Ignite在内存和磁盘上都维护相同的二进制数据表示形式，这样在内存和磁盘之间移动数据时就不需要进行昂贵的序列化。

#### 内存分区
Ignite使用数据区的概念来控制可用于缓存的内存数量，数据区是缓存数据存储在内存中的逻辑可扩展区域。
可以控制数据区的初始值及其可以占用的最大值，除了大小之外，数据区还控制缓存的持久化配置

**Ignite有一个默认的数据区最多可占用该节点20％的内存，并且创建的所有缓存均位于该数据区中**，
但是也可以添加任意多个数据区，创建多个数据区的原因有：

- 可以通过不同数据区分别配置缓存对应的可用内存量；
- 持久化参数是按数据区配置的。如果要同时具有纯内存缓存和持久化缓存，
    则需要配置两个（或多个）具有不同持久化参数的数据区：一个用于纯内存缓存，一个用于持久化缓存；
- 部分内存参数，比如退出策略，是按照数据区进行配置的。

### 纯内存模式 （无内存持久化）
操作系统、Ignite和其他应用之间会共享整个主机的内存。

**通常来说，如果以纯内存模式部署Ignite集群（禁用原生持久化），则不应为Ignite节点分配超过90％的内存量。**

### 持久化模式

如果使用了原生持久化，则操作系统需要额外的内存用于其页面缓存，以便以最佳方式将数据同步到磁盘。
**如果未禁用页面缓存，则不应将超过70％的内存分配给Ignite。**

除此之外，由于使用原生持久化可能会导致较高的页面缓存利用率，
因此kswapd守护进程可能无法跟上页面回收（页面缓存在后台使用）。
因此由于直接页面回收，可能导致高延迟，并导致长GC暂停。

要解决Linux上页面内存回收造成的影响，需要使用/proc/sys/vm/extra_free_kbytes在wmark_min和wmark_low之间添加额外的字节
```bash
sysctl -w vm.extra_free_kbytes=1240000
```


### java堆和GC
虽然Ignite将数据保存在Java垃圾收集器看不到的堆外数据区中，但Java堆仍用于由应用生成的对象。
例如每当执行SQL查询时，查询将访问堆外内存中存储的数据和索引，在应用读取结果集期间，此类查询的结果集会保留在Java堆中。
因此根据吞吐量和操作类型，仍然可以大量使用Java堆，这也需要针对工作负载进行JVM和GC相关的调优。

下面提供了一些常规的建议和最佳实践，可以以下面的配置为基础，然后根据实际情况做进一步的调整。

下面是一些应用的JVM配置集示例，这些应用会在服务端节点上大量使用Java堆，从而触发长时间或频繁的短期GC暂停。

对于JDK 1.8+环境，应使用G1垃圾收集器，如果10GB的堆可以满足服务端节点的需要，则以下配置是个好的起点：
```text
-server
-Xms10g
-Xmx10g
-XX:+AlwaysPreTouch
-XX:+UseG1GC
-XX:+ScavengeBeforeFullGC
-XX:+DisableExplicitGC

```

> **如果使用了Ignite原生持久化，建议将MaxDirectMemorySizeJVM参数设置为walSegmentSize * 4，对于默认的WAL设置，该值等于256MB**