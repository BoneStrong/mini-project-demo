### arthas的trace实现猜想
arthas追踪监控方法及方法体内部方法调用时，会找到当前ThreadLocal的traceTree
并把当前方法作为treeNode加入。

这里有一个问题就是，arthas是如何判断当前方法也是需要拦截的，比如arths要trace A(),A内部有B(),C(),D()
3个方法，执行到B()时，arthas凭啥拦截他。

假如字节码修改是一开始就插入的，在增强A方法体时就在内部的B,C,D前面加好了字节码。
那他是如何注入字节码的呢？

查看Asm实现可以看到，ASM可以静态代码解析方法依赖及生成方法调用流程图，
及通过ASM，我们就可以获取到A方法体里面所有的方法。（实现是methodInfo 里面有MethodInfo List）

这个时候实现链路大概就打通了，流程如下：
1. trace命令触发时，arthas通过 jvm attach目标jvm,触发load_onAttach事件，
    加载Instrumentation.
2. 这个Instrumentation 根据trace 设置的匹配参数获取目标类的方法信息集合
3. methodMatcher 通过asm 分析methodInfo 获取所有的method和相关class
4. Asm Enhance这些class的method ,为了扩展，这些增强部分都是实现成Interceptor
5. 目标JVM运行后 触发agentMian ,Instrumentation 重新进行类加载，把目标类都替换为增强类文件
6. trace 目标类方法运行时触发相关增强类的方法，打印trace链路

其他一些补充：
实际是A,B,C,D这些方法都是单独增强，要组成链路树还是需要traceId把链路串起来，
实现的时候是进入目标方法A时创建TraceNode，并生成TraceId,写入ThreadLocal，这样多线程
执行时触发相同的方法可以互不干扰TraceTree的统计，把整个链路给串起来了。

一些问题思考：
B,C,D方法如果是异步运行时，其实链路还是有问题，这个时候需要规范异步代码的编写，统一使用规范的线程池

