以上时序图做了些简化，去除了一些与重点无关的信息，
AsyncExecutionStrategy的execute方法是对象执行策略的异步化模式实现，
是查询执行的起点，也是根节点查询的入口，AsyncExecutionStrategy对对象的多个字段的查询逻辑，
采取的是循环+异步化的实现方式，我们从AsyncExecutionStrategy的execute方法触发，理解GraphQL查询过程如下：

1. 调用当前字段所绑定的DataFetcher的get方法，如果字段没有绑定DataFetcher，
   则通过默认的PropertyDataFetcher查询字段，PropertyDataFetcher的实现是基于反射从源对象中读取查询字段。

2. 将从DataFetcher查询得到结果包装成CompletableFuture，如果结果本身是CompletableFuture，那么不会包装。

3. 结果CompletableFuture完成之后，调用completeValue，基于结果类型分别处理。

- 如果查询结果是列表类型，那么会对列表类型进行遍历，针对每个元素在递归执行completeValue。

- 如果结果类型是对象类型，那么会对对象执行execute，又回到了起点，也就是AsyncExecutionStrategy的execute。

以上是GraphQL的执行过程，这个过程有什么问题呢？
下面基于图上的标记顺序一起看看GraphQL在我们的业务场景中应用和实践所遇到的问题，这些问题不代表在其他场景也是问题，仅供参考：

问题1：PropertyDataFetcherCPU热点问题，PropertyDataFetcher在整个查询过程中属于热点代码，
而其本身的实现也有一些优化空间，在运行时PropertyDataFetcher的执行会成为CPU热点。
（具体问题可参考GitHub上的commit和Conversion：https://github.com/graphql-java/graphql-java/pull/1815）


问题2：列表的计算耗时问题，列表计算是循环的，对于查询结果中存在大列表的场景，此时循环会造成整体查询明显的延迟。
我们举个具体的例子，假设查询结果中存在一个列表大小是1000，每个元素的处理是0.01ms，那么总体耗时就是10ms，
基于GraphQL的查机制，这个10ms会阻塞整个链路。

————————————————
版权声明：本文为CSDN博主「美团技术团队」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
原文链接：https://blog.csdn.net/MeituanTech/article/details/116468875