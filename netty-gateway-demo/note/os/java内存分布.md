JVM 进程占用大小一般约等于： heap + perm + thread stack + nio directbuffer
当然还有其他占用，一般情况来看native memory跟踪可以使用NMT参数 -XX:NativeMemoryTracking

jcmd pid  VM.native_memory detail scale=MB可以看出，
Java 进程内存包括：
- Java Heap: 堆内存，即-Xmx限制的最大堆大小的内存。 
- Class：加载的类与方法信息，其实就是 metaspace，包含两部分： 
  - 一是 metadata，被-XX:MaxMetaspaceSize限制最大大小 
  - 另外是 class space，被-XX:CompressedClassSpaceSize限制最大大小
    
- Thread：线程与线程栈占用内存，每个线程栈占用大小受-Xss限制，但是总大小没有限制。
  
- Code：JIT 即时编译后（C1 C2 编译器优化）的代码占用内存，受-XX:ReservedCodeCacheSize限制
  
- GC：垃圾回收占用内存，例如垃圾回收需要的 CardTable，标记数，区域划分记录，还有标记 GC Root 等等，都需要内存。这个不受限制，一般不会很大的。
  
- Compiler：C1、C2编译器本身的代码和标记占用的内存，这个不受限制，一般不会很大的.
  
- Internal：命令行解析，JVMTI等使用的内存，这个不受限制，一般不会很大的。
  > JVMTI(JVM Tool Interface) 位于jpda 最底层， 是Java 虚拟机所提供的native编程接口。
  > JVMTI可以提供性能分析、debug、内存管理、线程分析等功能。
  
- Symbol: 常量池占用的大小，字符串常量池受-XX:StringTableSize个数限制，总内存大小不受限制。
  
- Native Memory Tracking：内存采集本身占用的内存大小，如果没有打开采集（那就看不到这个了，哈哈），就不会占用，这个不受限制，一般不会很大的.

- Arena Chunk：所有通过 arena 方式分配的内存，这个不受限制，一般不会很大的 
> https://xie.infoq.cn/article/e6fa07979e4375367ae9c2795 对arena内存分配有相应描述
  
- Tracing：所有采集占用的内存，如果开启了 JFR 则主要是 JFR 占用的内存。这个不受限制，一般不会很大的
  
- Logging，Arguments，Module，Synchronizer，Safepoint，Other，这些一般我们不会关心。
  
除了 Native Memory Tracking 记录的内存使用，还有两种内存 Native Memory Tracking 没有记录，那就是：
1. Direct Buffer：直接内存，请参考：JDK核心JAVA源码解析（4） - Java 堆外内存、零拷贝、直接内存以及针对于NIO中的FileChannel的思考
2. MMap Buffer：文件映射内存，请参考：JDK核心JAVA源码解析（5） - JAVA File MMAP原理解析


### JVM 的内存大概分为下面这几个部分
- 堆（Heap）：eden、metaspace、old 区域等
- 线程栈（Thread Stack）：每个线程栈预留 1M 的线程栈大小
- 非堆（Non-heap）：包括 code_cache、metaspace 等
- 堆外内存：unsafe.allocateMemory 和 DirectByteBuffer 申请的堆外内存
- native （C/C++ 代码）申请的内存
- 还有 JVM 运行本身需要的内存，比如 GC 等。

接下来怀疑堆外内存和 native 内存可能存在泄露问题。堆外内存可以通过开启 NMT（NativeMemoryTracking) 来跟踪，
加上 -XX:NativeMemoryTracking=detail 再次启动程序，也发现内存占用值远小于 RES 内存占用值。

因为 NMT 不会追踪 native （C/C++ 代码）申请的内存，到这里基本已经怀疑是 native 代码导致的。
我们项目中除了 rocksdb 用到了 native 的代码就只剩下 JVM 自己了。接下来继续排查。