### 应用日志和dump日志
有时候OOM查看java日志可以直接看到OOM异常原因，
但是有时候是看不出来的，比如往list里面循环放数据，最后添加一个元素的时候OOM。
这个时候日志不一定直观。

所以为了防患于未然，开发的时候，一定要配置jvm启动参数HeapDumpOnOutOfMemoryError。
参数-XX：+HeapDumpOnOutOfMemoryError
可以让虚拟机在出现内存溢出异常时Dump出当前的内存堆转储快照以便事后进行分析。

### linux系统日志
有些时候，我们的应用程序宕机，既不会打印log日常信息，dump文件也不会生成，
这个时候基本就是linux系统杀掉了我们的应用程序进程。

**查看/var/log/messages文件**

messages 日志是核心系统日志文件。它包含了系统启动时的引导消息，以及系统运行时的其他状态消息。
在messages里会出现以下信息
```text
out of memory:kill process 8398(java) score 699 or sacrifice child
killed process 8398,UID 505,(java) total-vm:5212216kB,anno-rss:2785556kB,file-rss:0kB,shmem-rss:0KB
```
其中
- total-vm 表示如果一个程序完全驻留在内存的话需要占用多少内存空间,此处为还需要申请4.97GB
- anon-rss 进程当前实际占用了多少内存，此处为占用了2.67GB 

oom killer是linux系统的一个保护进程，当linux系统所剩的内存空间不足以满足系统正常运行时，会触发。
oomkiller执行时，会找出系统所有线程的score值最高的那个pid，然后干掉。
这里我们可以看到，JAVA进程的确是被LINUX的oom killer干掉了。

我们的应用程序和日志都只能记录JVM内发生的内存溢出。
如果JVM设置的堆大小超出了操作系统允许的内存大小，那么操作系统会直接杀死进程，这种情况JVM就无法记录本次操作。
Linux对于每个进程有一个OOM评分，这个评分在/proc/pid/oom_score文件中。
例如/proc/8398/oom_score，如果不希望杀死这个进程，就将oom_adj内容改为-17。
更多关于linux的oom killer机制请自行百度检索。

最正确的姿势:首先调整JVM的heap大小，使得JVM的OOM优先于操作系统的OOM出现，接着设置运行参数，在发生OOM的时候输出heapdump文件。



## 一些案例
一次内存高频置换导致的CPU利用率高的问题
现象： 收到业务CPU利用率高告警，最后触发OOM机制，进程被杀重启
定位结论： 目前的配置（APP组件当前资源配置为2C4G，JVM配置为2G）不满足业务稳定运行的需要，导致资源持续高负载，
最后由于内存不足触发系统OOM； 被系统内核杀掉后，被自愈脚本重新拉起的方式，实现业务恢复和资源利用率暂时下降 。

处理方案：
1． 远期：对当前APP组件的业务进行整理，还请酌情考虑是否可以分离部分功能或者组件微服务化。
    - 日志转存到日志服务器。日志分割和压缩转移到远端服务器
    - 对一些大数据的查询，建议前端做好分页展示，后端分页查询，避免一次性加载过多数据导致OOM

2． 短期：日志级别降低和取消部分代码打印。，确保业务稳定持续运行。

3． 云主机扩容

## 案例总结

CPU过高场景：
- 信息落盘量大、网卡吞吐大的应用产生了中断较多，cpu需要保护和切换上下文消耗
- 内存利用率过高，触发内核内存回收，消耗1个逻辑核的代价；

Java应用排查的通用工具：
- JVM和JDK自带的配套CS监控工具：Jmx+jconsole
- 阿里巴巴开源的arthas在线分析jvm信息
- Jdk自带的jstack、jmap、jstat已经配套的MAT分析工具
- Perf内核级别的调用查看工具

java OOM场景情况归类：
- 数据查询总量过大，导致内存申请过高触发OOM
- JVM配置过低，或者主机配置过低
- 代码出现死循环出现很多重复对象的内存占用 、使用后未正常释放、

Java应用分析通用思路：

1． top查找资源利用率最高的进程
2． 获取该进程的线程的资源消耗pidstat -t -p或者top –H
3． perf top -p 27533 看进程具体对象调用情况
4． 根据top信息，获取线程的详细堆栈信息。jstack或者arthas的thread和sm

5． 获取对象类名class和方法method后，用arthas定位其调用路径
> stack  org.LatencyUtils.SimplePauseDetector$SimplePauseDetectorThread run  >stack.log &

6． 如果是内存占用具体情况和内存泄露等还需要结合jmap dump信息，使用MAT工具进行综合分析
7． 期间可能还需要观察网卡和CPU、存储等信息综合分析mpstat  iostat  sar  iotop iftop等信息查看

