### MappedFileQueue
MappedFileQueue是MappedFile的管理容器，MappedFileQueue是对存储目录的封装。MappedFileQueue类的核心属性如下：

```text
private final String storePath; // 存储目录
private final int mappedFileSize; // 单个文件的存储大小
private final CopyOnWriteArrayList<MappedFile> mappedFiles = new CopyOnWriteArrayList<MappedFile>(); //MappedFile文件集合
private final AllocateMappedFileService allocateMappedFileService; // 创建MappedFile服务类
private long flushedWhere = 0; // 当前刷盘指针，表示该指针之前的所有数据全部持久化到磁盘
private long committedWhere = 0; // 当前数据提交指针，内存中ByteBuffer当前的写指针，该值大于等于flushedWhere
private volatile long storeTimestamp = 0; // 刷盘时间戳
```

知道了MappedFileQueue的存储内容之后，让我们来看看通过它，我们都能做什么。 
通过时间查找消息所在的文件 从MappedFile列表中第一个文件开始查找，找到第一个最后一次更新时间大于待查找时间戳的文件，
如果不存在，则返回最后一个MappedFile文件。 
通过偏移量查找消息所在的文件 因为RocketMQ会定时清除过期的数据，所以第一个MappedFile对应的偏移量不一定是00000000000000000000，
所以根据偏移量计算文件位置的算法为：查找偏移量/单个文件的大小 - 第一个文件的起始偏移量/单个文件的大小

### MappedByteBuffer
在深入MappedByteBuffer之前，先看看计算机内存管理的几个术语：
- MMC：CPU的内存管理单元。
- 物理内存：即内存条的内存空间。
- 虚拟内存：计算机系统内存管理的一种技术。它使得应用程序认为它拥有连续的可用的内存（一个连续完整的地址空间），
    而实际上，它通常是被分隔成多个物理内存碎片，还有部分暂时存储在外部磁盘存储器上，在需要时进行数据交换。
- 页面文件：物理内存被占满后，将暂时不用的数据移动到硬盘上。
- 缺页中断：当程序试图访问已映射在虚拟地址空间中但未被加载至物理内存的一个分页时，由MMC发出的中断。
  如果操作系统判断此次访问是有效的，则尝试将相关的页从虚拟内存文件中载入物理内存。
  
如果正在运行的一个进程，它所需的内存是有可能大于内存条容量之和的，如内存条是256M，程序却要创建一个2G的数据区，
那么所有数据不可能都加载到内存（物理内存），必然有数据要放到其他介质中（比如硬盘），待进程需要访问那部分数据时，再调度进入物理内存。

假设你的计算机是32位，那么它的地址总线是32位的，也就是它可以寻址0xFFFFFFFF（4G）的地址空间，
但如果你的计算机只有256M的物理内存0x0FFFFFFF（256M），同时你的进程产生了一个不在这256M地址空间中的地址，那么计算机该如何处理呢？

计算机会对虚拟内存地址空间（32位为4G）进行分页，从而产生页（page），对物理内存地址空间（假设256M）进行分页产生页帧（page frame），
页和页帧的大小一样，所以虚拟内存页的个数势必要大于物理内存页帧的个数。在计算机上有一个页表（page table），
就是映射虚拟内存页到物理内存页的，更确切的说是页号到页帧号的映射，而且是一对一的映射。

那么问题来了，虚拟内存页的个数 > 物理内存页帧的个数，岂不是有些虚拟内存页的地址永远没有对应的物理内存地址空间？
不是的，操作系统是这样处理的：如果要用的页没有找到，操作系统会触发一个页面失效（page fault）功能，操作系统找到一个最少使用的页帧，
使之失效，并把它写入磁盘，随后把需要访问的页放到页帧中，并修改页表中的映射，保证了所有的页都会被调度。

FileChannel提供了map方法把文件映射到虚拟内存：
```java
// 只保留了核心代码
public MappedByteBuffer map(MapMode mode, long position, long size)  throws IOException {
    // allocationGranularity一般等于64K，它是虚拟内存的分配粒度，由操作系统指定
    // 这里将position与分配粒度取余，然后真实映射起始位置为mapPosition = position-pagePosition,position 是参数指定的 position，pagePosition是根据内存分配粒度取余的结果，最终算出映射起始地址，这样算是为了内存对齐
    // 这样无论position为多少，得出的各个MappedByteBuffer实例之间的内存都是成块对齐的
    // 对齐的好处：如果两个不同的MappedByteBuffer，即便它们的position不同，但是只要它们有公共映射区域的话，这些公共区域在物理内存上的分页会被共享
    // 如果它们的MapMode是PRIVATE的话，那么会copy-on-write的方式来对修改内容进行私有化
    // 而如果它们的MapMode是SHARED的话，那么对映射的修改，其他实例均可见
    // 实际上，上述的过程都是内核来做的，我们要做的只是调用map0时将对齐好的position输入即可，这实际上是map0下层使用的mmap系统调用的约束
    int pagePosition = (int)(position % allocationGranularity);
    long mapPosition = position - pagePosition;
    long mapSize = size + pagePosition;
    try {
        addr = map0(imode, mapPosition, mapSize);
    } catch (OutOfMemoryError x) {
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException y) {
            Thread.currentThread().interrupt();
        }
        try {
            addr = map0(imode, mapPosition, mapSize);
        } catch (OutOfMemoryError y) {
            // After a second OOME, fail
            throw new IOException("Map failed", y);
        }
    }
    int isize = (int)size;
    Unmapper um = new Unmapper(addr, mapSize, isize, mfd);
    if ((!writable) || (imode == MAP_RO)) {
        return Util.newMappedByteBufferR(isize,
                                         addr + pagePosition,
                                         mfd,
                                         um);
    } else {
        return Util.newMappedByteBuffer(isize,
                                        addr + pagePosition,
                                        mfd,
                                        um);
    }
}
```

由于FileChannelImpl和DirectByteBuffer不在同一个包中，所以有权限访问问题，通过AccessController类获取DirectByteBuffer的构造器进行实例化。

map0()函数返回一个虚拟内存地址address，这样就无需调用read或write方法对文件进行读写，通过address就能够操作文件。
底层采用unsafe.getByte方法，通过（address + 偏移量）获取指定内存的数据。

第一次访问address所指向的内存区域，导致缺页中断，中断响应函数会在交换区中查找相对应的页面，
如果找不到（也就是该文件从来没有被读入内存的情况），则从硬盘上将文件指定页读取到物理内存中（非jvm堆内存）。
如果在拷贝数据时，发现物理内存不够用，则会通过虚拟内存机制（swap）将暂时不用的物理页面交换到硬盘的虚拟内存中。
MappedByteBuffer的效率之所以比read/write高，主要是因为read/write过程会涉及到用户内存拷贝到内核缓冲区，
而MappedByteBuffer在发生缺页中断时，直接将硬盘内容拷贝到了用户内存，这也就是我们所说的零拷贝技术。
所以，采用内存映射的读写效率要比传统的read/write性能高。

MappedByteBuffer使用虚拟内存，因此分配(map)的内存大小不受JVM的-Xmx参数限制，但是也是有大小限制的。
如果当文件超出大小限制Integer.MAX_VALUE时，可以通过position参数重新map文件后面的内容。

至此，我们已经了解了文件内存映射的技术，既然Java已经提供了内存映射的方案，还有MappedFile什么事呢？
这一层封装又有何意义呢？接下来再回到MappedFile的介绍中来，我将详细介绍RocketMQ的MappedFile都对原生内存映射方案做了哪些增强。

### 初始化
在不开启RocketMQ的内存映射增强方案时，它会规规矩矩地使用Java的MappedByteBuffer。

通过RandomAccessFile创建读写文件通道，并将文件内容使用NIO的内存映射将文件映射到内存中，最后得到的就是MappedByteBuffer实例。
随后介绍数据存储的时候，你就会发现在不开启RocketMQ内存映射优化时，它都是对mappedByteBuffer进行写入和刷盘。

我们知道，MappedByteBuffer已经很快了，已经是零拷贝了，还有什么可以优化的呢？在前面对MappedByteBuffer的介绍中，我们知道它实际上使用的是虚拟内存，
当虚拟内存的使用超过物理内存大小时，势必会造成内存交换，这就会导致在内存使用的过程中进行磁盘IO，而且它不一定是顺序磁盘IO，所以会很慢。
而且虚拟内存的交换是由操作系统控制的，系统中的其他进程活动，也会触发RocketMQ内存映射的内存交换。
此外，因为**文件内存映射的写入过程实际上是写入 PageCache，这就涉及到 PageCache 的锁竞争，而如果直接写入内存的话就不存在该竞争**，
在异步刷盘的场景下可以达到更快的速度。综上RocketMQ就对其进行了优化，该优化使用transientStorePoolEnable参数控制。

> 也就是说自己申请的directBuffer 不像是mmap map出来的directBuffer是和pageCache直接映射的

如果transientStorePoolEnable为true，则初始化MappedFile的writeBuffer，该buffer从transientStorePool中获取。

那么TransientStorePool中拿到的buffer和MappedByteBuffer又有什么区别呢？这就得看看transientStorePool的代码了。

```text
// TransientStorePool初始化过程
public void init() {
for (int i = 0; i < poolSize; i++) {
ByteBuffer byteBuffer = ByteBuffer.allocateDirect(fileSize);
final long address = ((DirectBuffer) byteBuffer).address();
Pointer pointer = new Pointer(address);
LibC.INSTANCE.mlock(pointer, new NativeLong(fileSize)); // 加锁后，该内存就不会发生交换
availableBuffers.offer(byteBuffer);
}
}
```

从的代码，我们可以看出该内存池的内存实际上用的也是直接内存，把要存储的数据先存入该buffer中，然后再需要刷盘的时候，
将该buffer的数据传入FileChannel，这样就和MappedByteBuffer一样能做到零拷贝了。
除此之外，该Buffer还使用了com.sun.jna.Library类库将该批内存锁定，避免被置换到交换区，提高存储性能。

至此，我们已经知道了RocketMQ根据配置的不同，可能会使用来自TransientStorePool的writeBuffer或者MappedByteBuffer来存储数据，
接下来，我们就来看一看存储数据的过程是如何实现的。

MappedFile插入数据
这里所指的插入数据，是在内存层面将要存储的数据加入到MappedFile的Buffer中，核心实现逻辑在appendMessagesInner：

```text
public AppendMessageResult appendMessagesInner(final MessageExt messageExt, final AppendMessageCallback cb) {
assert messageExt != null;
assert cb != null;

     int currentPos = this.wrotePosition.get();

     if (currentPos < this.fileSize) {
         ByteBuffer byteBuffer = writeBuffer != null ? writeBuffer.slice() : this.mappedByteBuffer.slice();
         byteBuffer.position(currentPos);
         AppendMessageResult result;
         if (messageExt instanceof MessageExtBrokerInner) {
             result = cb.doAppend(this.getFileFromOffset(), byteBuffer, this.fileSize - currentPos, (MessageExtBrokerInner) messageExt);
         } else if (messageExt instanceof MessageExtBatch) {
             result = cb.doAppend(this.getFileFromOffset(), byteBuffer, this.fileSize - currentPos, (MessageExtBatch) messageExt);
         } else {
             return new AppendMessageResult(AppendMessageStatus.UNKNOWN_ERROR);
         }
         this.wrotePosition.addAndGet(result.getWroteBytes());
         this.storeTimestamp = result.getStoreTimestamp();
         return result;
     }
     log.error("MappedFile.appendMessage return null, wrotePosition: {} fileSize: {}", currentPos, this.fileSize);
     return new AppendMessageResult(AppendMessageStatus.UNKNOWN_ERROR);
}
```

从第八行我们可以看到，如果writeBuffer不为空，说明使用了TransientStorePool，则使用writeBuffer作为写入时使用的buffer，
否则使用mappedByteBuffer。然后根据当前的写指针wrotePosition设置buffer的position，
而实际的写入过程在AppendMessageCallback::doAppend中。写入完成后更新写指针wrotePosition和存储时间戳。

> slice() 方法创建一个共享缓存区，与原先的ByteBuffer共享内存但维护一套独立的指针(position、mark、limit)。

### MappedFile提交
MappedFile提交实际上是将writeBuffer中的数据，传入FileChannel，所以只有在transientStorePoolEnable为true时才有实际作用：

```text
public int commit(final int commitLeastPages) {
if (writeBuffer == null) {
//no need to commit data to file channel, so just regard wrotePosition as committedPosition.
return this.wrotePosition.get();
}
if (this.isAbleToCommit(commitLeastPages)) {
if (this.hold()) {
commit0(commitLeastPages);
this.release();
} else {
log.warn("in commit, hold failed, commit offset = " + this.committedPosition.get());
}
}
// All dirty data has been committed to FileChannel.
if (writeBuffer != null && this.transientStorePool != null && this.fileSize == this.committedPosition.get()) {
this.transientStorePool.returnBuffer(writeBuffer);
this.writeBuffer = null;
}
return this.committedPosition.get();
}
commitLeastPagesTransientStorePool为本次提交最小的页数，如果待提交数据不满commitLeastPages，则不执行本次提交操作，待下次提交。writeBuffer如果为空，直接返回wrotePosition指针，无须执行commit操作，正如前面所说，commit操作主体是writeBuffer。

private boolean isAbleToFlush(final int flushLeastPages) {
int flush = this.flushedPosition.get();
int write = getReadPosition();
if (this.isFull()) {
return true;
}
if (flushLeastPages > 0) {
return ((write / OS_PAGE_SIZE) - (flush / OS_PAGE_SIZE)) >= flushLeastPages;
}
return write > flush;
}
判断是否执行commit操作。如果文件己满返回true;如果commitLeastPages大于0, 则比较wrotePosition(当前writeBuffer的写指针)与上一次提交的指针(committedPosition)的差值，除以OS_PAGE_SIZE得到当前脏页的数量，如果大于commitLeastPages则返回true;如果commitLeastPages小于0表示只要存在脏页就提交。

protected void commit0(final int commitLeastPages) {
int writePos = this.wrotePosition.get();
int lastCommittedPosition = this.committedPosition.get();
if (writePos - this.committedPosition.get() > 0) {
try {
ByteBuffer byteBuffer = writeBuffer.slice();
byteBuffer.position(lastCommittedPosition);
byteBuffer.limit(writePos);
this.fileChannel.position(lastCommittedPosition);
this.fileChannel.write(byteBuffer);
this.committedPosition.set(writePos);
} catch (Throwable e) {
log.error("Error occurred when commit data to FileChannel.", e);
}
}
}
```

具体的提交实现。首先创建writeBuffer的共享缓存区，然后将新创建的buffer position回退到上一次提交的位置(committedPosition)，
设置limit为wrotePosition(当前最大有效数据指针)，然后把committedPosition到wrotePosition的数据复制(写入)到FileChannel中，
然后更新committedPosition指针为wrotePosition,commit的作用就是将writeBuffer中的数据提交到文件通道FileChannel中,
CommitLog在采用异步存储方式时，会有一个后台任务循环的进行commit操作，如果进行同步存储，也会主动调用MappedFile的commit，随后再调用flush刷盘。

### MappedFile刷盘
刷盘指的是将内存中的数据刷写到磁盘，永久存储在磁盘中，其具体实现由MappedFile的flush方法实现，如下所示。

```text
public int flush(final int flushLeastPages) {
if (this.isAbleToFlush(flushLeastPages)) {
if (this.hold()) {
int value = getReadPosition();

            try {
                //We only append data to fileChannel or mappedByteBuffer, never both.
                if (writeBuffer != null || this.fileChannel.position() != 0) {
                    this.fileChannel.force(false);
                } else {
                    this.mappedByteBuffer.force();
                }
            } catch (Throwable e) {
                log.error("Error occurred when force data to disk.", e);
            }

            this.flushedPosition.set(value);
            this.release();
        } else {
            log.warn("in flush, hold failed, flush offset = " + this.flushedPosition.get());
            this.flushedPosition.set(getReadPosition());
        }
    }
    return this.getFlushedPosition();
}
```

flush函数和commit一样也可以传入一个刷盘页数，当脏页数量达到要求时，会进行刷盘操作，
如果使用writeBuffer存储的话则调用fileChannel的force将内存中的数据持久化到磁盘，刷盘结束后，
flushedPosition会等于committedPosition，否则调用mappedByteBuffer的force，最后flushedPosition会等于writePosition。

我们不妨分析一下wrotePosition，committedPosition，flushedPosition的关系，当有新的数据要写入时，先会写入内存，
然后writePosition代表的就是内存写入的末尾，commit过程只有transientStorePoolEnable为true时才有意义，
代表的是从writeBuffer拷贝到FileChannel时，拷贝数据的末尾，而flushedPosition则代表将内存数据刷盘到物理磁盘的末尾。

综上所述，我们可以得到一个关于这三个position之间的关系：

>transientStorePoolEnable: flushedPosition<=committedPosition<=wrotePosition
MappedByteBuffer only: flushedPosition<=wrotePosition

获取MappedFile最大读指针
RocketMQ文件的一个组织方式是内存映射文件，预先申请一块连续的固定大小的内存，需要一套指针标识当前最大有效数据的位置，
获取最大有效数据偏移量的方法由MappedFile的getReadPosition方法实现，如下所示。

```text
/**
* @return The max position which have valid data
  */
  public int getReadPosition() {
  return this.writeBuffer == null ? this.wrotePosition.get() : this.committedPosition.get();
  }
```

获取当前文件最大的可读指针。如果 writeBuffer 为空，则直接返回当前的写指针;
如果 writeBuffer 不为空，则返回上一次提交的指针。
在 MappedFile 设计中，只有提交了的数据(写入到 MappedByteBuffer 或 FileChannel 中的数据)才是安全的数据。
为什么没刷盘之前也认为是安全数据呢，这就和 MappedByteBuffer 和 FileChannel 的写入机制有关了，
无论是 MappedByteBuffer 还是 FileChannel 在写入数据时，实际上只是将数据写入 PageCache，而操作系统会自动的将脏页刷盘，
这层 PageCache 就是我们应用和物理存储之间的夹层，当我们将数据写入 PageCache 后，即便我们的应用崩溃了，但是只要系统不崩溃，
最终也会将数据刷入磁盘。所以，RocketMQ 以写入 PageCache 作为数据安全可读的判断标准。

### 读取数据
RocketMQ 在读数据时，使用的是 MappedByteBuffer，并且以最大读指针作为可读数据的末尾。
之所以使用MappedByteBuffer而不是FileChannel主要是因为它更快，这一点在后面的各种流速度对比中就能看到。

```text
public SelectMappedBufferResult selectMappedBuffer(int pos, int size) {
int readPosition = getReadPosition();
if ((pos + size) <= readPosition) {
if (this.hold()) {
ByteBuffer byteBuffer = this.mappedByteBuffer.slice();
byteBuffer.position(pos);
ByteBuffer byteBufferNew = byteBuffer.slice();
byteBufferNew.limit(size);
return new SelectMappedBufferResult(this.fileFromOffset + pos, byteBufferNew, size, this);
} else {
log.warn("matched, but hold failed, request pos: " + pos + ", fileFromOffset: "
+ this.fileFromOffset);
}
} else {
log.warn("selectMappedBuffer request pos invalid, request pos: " + pos + ", size: " + size
+ ", fileFromOffset: " + this.fileFromOffset);
}
return null;
}
```

  

### MappedFile销毁
为了保证 MappedFile 在销毁的时候，不对正在进行的读写造成影响，所以 MappedFile 实际上还是一个计数引用资源，
每当要进行读写操作时，都需要调用其 hold 函数，当使用完成后需要主动调用 release 函数释放资源。

```text
// ReferenceResource
// 默认引用数为1，当需要销毁时调用release将其减为0，最后释放资源
protected final AtomicLong refCount = new AtomicLong(1);
// 标识资源是否可用（未被销毁）
protected volatile boolean available = true;
// 每当持有资源时，引用数加一，如果发现已经不可用就回退，这里用双层检验保证线程安全：1.isAvailable（）2.this.refCount.getAndIncrement() > 0
public synchronized boolean hold() {
if (this.isAvailable()) {
if (this.refCount.getAndIncrement() > 0) {
return true;
} else {
this.refCount.getAndDecrement();
}
}
return false;
}
// 释放资源，如果引用数小于0，则开始销毁逻辑
public void release() {
long value = this.refCount.decrementAndGet();
if (value > 0)
return;
synchronized (this) {
this.cleanupOver = this.cleanup(value);
}
}
// 主动触发销毁过程，实际上会调用 release 函数来进行销毁，这里如果销毁失败，会在每次尝试销毁时，按照一定的时间间隔，将引用数-1000来强制进行销毁。
public void shutdown(final long intervalForcibly) {
if (this.available) {
this.available = false;
this.firstShutdownTimestamp = System.currentTimeMillis();
this.release();
} else if (this.getRefCount() > 0) {
if ((System.currentTimeMillis() - this.firstShutdownTimestamp) >= intervalForcibly) {
this.refCount.set(-1000 - this.getRefCount());
this.release();
}
}
}
```


MappedFile 的销毁就是通过调用 ReferenceResource 的shutdown来实现的，实际上 MappedFile 是 ReferenceResource 的子类，
并实现了其 cleanup 函数。综上所述，MappedFile 的销毁过程就是：
MappedFile::destroy -> ReferenceResource::shutdown -> ReferenceResource::release -> MappedFile::cleanup。

```text
public boolean destroy(final long intervalForcibly) {
this.shutdown(intervalForcibly);
if (this.isCleanupOver()) {
try {
this.fileChannel.close();
log.info("close file channel " + this.fileName + " OK");
long beginTime = System.currentTimeMillis();
boolean result = this.file.delete();
log.info("delete file[REF:" + this.getRefCount() + "] " + this.fileName
+ (result ? " OK, " : " Failed, ") + "W:" + this.getWrotePosition() + " M:"
+ this.getFlushedPosition() + ", "
+ UtilAll.computeElapsedTimeMilliseconds(beginTime));
} catch (Exception e) {
log.warn("close file channel " + this.fileName + " Failed. ", e);
}
return true;
} else {
log.warn("destroy mapped file[REF:" + this.getRefCount() + "] " + this.fileName
+ " Failed. cleanupOver: " + this.cleanupOver);
}
return false;
}
```

  
MappedByteBuffer 的释放过程实际上有些诡异，Java官方没有提供公共的方法来进行 MappedByteBuffer 的回收，
所以不得不通过反射来进行回收，这也是 MappedByteBuffer 比较坑的一点，我们不妨简单看下 MappedFile 的 cleanup 逻辑。

```text
public boolean cleanup(final long currentRef) {
if (this.isAvailable()) {
log.error("this file[REF:" + currentRef + "] " + this.fileName
+ " have not shutdown, stop unmapping.");
return false;
}
if (this.isCleanupOver()) {
log.error("this file[REF:" + currentRef + "] " + this.fileName
+ " have cleanup, do not do it again.");
return true;
}
clean(this.mappedByteBuffer);
TOTAL_MAPPED_VIRTUAL_MEMORY.addAndGet(this.fileSize * (-1));
TOTAL_MAPPED_FILES.decrementAndGet();
log.info("unmap file[REF:" + currentRef + "] " + this.fileName + " OK");
return true;
}
public static void clean(final ByteBuffer buffer) {
if (buffer == null || !buffer.isDirect() || buffer.capacity() == 0)
return;
invoke(invoke(viewed(buffer), "cleaner"), "clean");
}

private static Object invoke(final Object target, final String methodName, final Class<?>... args) {
return AccessController.doPrivileged(new PrivilegedAction<Object>() {
public Object run() {
try {
Method method = method(target, methodName, args);
method.setAccessible(true);
return method.invoke(target);
} catch (Exception e) {
throw new IllegalStateException(e);
}
}
});
}

private static Method method(Object target, String methodName, Class<?>[] args)
throws NoSuchMethodException {
try {
return target.getClass().getMethod(methodName, args);
} catch (NoSuchMethodException e) {
return target.getClass().getDeclaredMethod(methodName, args);
}
}

private static ByteBuffer viewed(ByteBuffer buffer) {
String methodName = "viewedBuffer";
Method[] methods = buffer.getClass().getMethods();
for (int i = 0; i < methods.length; i++) {
if (methods[i].getName().equals("attachment")) {
methodName = "attachment";
break;
}
}

    ByteBuffer viewedBuffer = (ByteBuffer) invoke(buffer, methodName);
    if (viewedBuffer == null)
        return buffer;
    else
        return viewed(viewedBuffer);
}
```

从上面的代码中我们可以看出 cleanup 先是进行了一些验证，然后就通过多个反射过程进行 MappedByteBuffer 的回收。

### 对比测试
看完了内存映射和 FileChannel 的使用，我不禁有一个疑问，它们到底哪个更快呢？自己的RocketMQ集群应该使用哪种策略呢？
于是找到了别人做的测试，该测试的环境如下：

CPU：intel i7 4核8线程 4.2GHz
内存：40GB DDR4
磁盘：SSD 读写 2GB/s 左右
JDK：1.8
OS：Mac OS 10.13.6
虚拟内存：9GB
测试注意点：

为了防止 PageCache 缓存的影响，每次都生成一个新的文件进行读取。
为了测试不同数据包对性能的影响，需要使用不同大小的数据包进行多次测试。
force 对性能影响很大，应该单独测试。
使用 1GB 文件进行测试（小文件没有参考意义，大文件 mmap 无法映射）
该测试是在Mac上进行的，在此只做参考使用，在实际部署RocketMQ生产集群时，还应根据实际部署物理机情况进行更深入的测试，
最终决定是否开启transientStorePoolEnable。


上图是读测试，从这张图里，我们看到，mmap 性能完胜，特别是在小数据量的情况下。其他的流，只有在读 4kb 的情况下，其他流才开始反杀 mmap。
因此，读 4kb 以下的数据，mmap 更优。而消息队列中存储的消息，一般来说都是比较小的，而且RocketMQ限制了消息的最大长度为4M。
基于这样的读数据场景，RocketMQ在读数据时，直接使用的是MappedByteBuffer（MMAP），这种选择和这次测试的效果是对应上的。


接下来看一下直接写的测试，64字节 是 FileChannel 和 mmap 性能的分水岭，从64字节开始，FileChannel 一路反杀，
直到 1GB 文件稍稍输了一丢丢。图中我们可以看到，在存储块的大小为16K时，FileChannel 的 write 效率最高，
不知道大家还记不记得，前面在介绍 MappedFile 的 commit 函数时，说过 commit 函数有一个 commitLeastPages，
当内存中的数据大于设定的页数（一页4K）时，才会将内存数据写入FileChannel。根据图中的数据，我们可以大胆的估计，
这个 commitLeastPages 等于 4 时，效率应该是最高的。然后我们回到 RocketMQ的代码中来，
我们会发现 RocketMQ 中 commitCommitLogLeastPages 的默认值就是 4。可见，RocketMQ 的默认设定很可能就是根据实际测试情况调优过的。

此外，我还找到了另一个同学的测试，它测试了SSD云盘的写入效率：


从图中，我们可以看出当数据块大小大于 16K 时，IO 吞吐量开始接近饱和。这两次测试都是在 SSD 上进行的，
它们都不约而同的在写入块为 16K 时达到了极高的写入效率。我猜测，它们使用的 SSD 页大小就是 16K。
之前在总结MySQL时，看到有些 SSD 的页大小是 16K，从而达到 16K 的原子写，这样就和 InnoDB 的页大小一致，
从而可以省略双写过程。扯的有点远了，让我们再回到测试中来，看看异步刷盘的效率。


从这张图中，我们可以看到MMAP的效率就没有FileChannel高了，此外，FileChannel 又是在写入块大小为16K时，达到了最高效率。
和之前一样，我们可以大胆假设异步刷盘过程很可能也是出现 4 个脏页（4 * 4K = 16K）时，RocketMQ才会进行异步刷盘。
再次，回到RocketMQ的代码中来，很快我就发现其默认刷盘页数 flushCommitLogLeastPages 就是 4。

到此为止，本节的测试和 RocketMQ 的默认策略就全都对上了，从读数据采用MMAP技术，再到使用 transientStorePool 异步写入的时机（16K 脏数据），
再到异步刷盘的时机（16K 脏页），RocketMQ 在性能优化的路上，真可谓不遗余力，不愧是扛起数次双11高压的核心中间件。

文章说明
更多有价值的文章均收录于贝贝猫的文章目录