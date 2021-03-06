package com.dzz.consistency.raft;

/**
 * @author zoufeng
 * @date 2020-8-21
 * 网上看到一篇文章，谈论的是OceanBase的一致性协议为什么选择 paxos而不是raft?
 *
 * 作者：正祥
 * 链接：https://www.zhihu.com/question/52337912/answer/131507725
 * 来源：知乎
 * 著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。
 *
 * 基于Raft的分布式一致性协议实现的局限及其对数据库的风险
 *
 * 普通服务器具有良好的性价比，因此在互联网等行业得到了广泛的应用。但普通服务器也不得不面对2%-4%的年故障率([1])，
 * 于是必须高可用的传统数据库只得很悲催地使用性价比低得可怜的高可靠服务器。
 * 分布式一致性协议（distributed consensus protocol）是迄今为止最有效的解决服务器不可靠问题的途径，
 * 因为它使得一组服务器形成一个相互协同的系统，从而当其中部分服务器故障后，整个系统也能够继续工作。
 * 而Paxos协议([2])则几乎成了分布式一致性协议的代名词。然而，Paxos协议的难以理解的名声似乎跟它本身一样出名。
 * 为此，Stanford大学的博士生Diego Ongaro甚至把对Paxos协议的研究作为了博士课题。他在2014年秋天正式发表了博士论文：
 * “CONSENSUS: BRIDGING THEORY AND PRACTICE”，在这篇博士论文中，他给出了分布式一致性协议的一个实现算法，即Raft。
 * 由于这篇博士论文很长（257页），可能是为了便于别人阅读和理解，他在博士论文正式发表之前，即2014年初，把Raft相关的部分摘了出来，
 * 形成了一篇十多页的文章：“In Search of an Understandable Consensus Algorithm”，即人们俗称的Raft论文。
 * Raft算法给出了分布式一致性协议的一个比较简单的实现，到目前为止并没有人挑战这个算法的正确性。
 *
 * 然而，OceanBase却没有采用Raft算法，这并非是OceanBase团队同学不懂Raft，而是Raft的一个根本性的局限对数据库的事务有很大的风险。
 * Raft有一个很强的假设是主（leader）和备（follower）都按顺序投票，
 * 为了便于阐述，以数据库事务为例：
 * ·主库按事务顺序发送事务日志
 * ·备库按事务顺序持久化事务和应答主库
 * ·主库按事务顺序提交事务
 *
 * 由于不同的事务可能被不同工作线程处理，事务日志可能被不同的网络线程发送和接收，因为网络抖动和Linux线程调度等原因，
 * 一个备库可能会出现接收到了事务日志#5-#9，但没有接收到事务#4，因此#5-#9的所有事务都需要hold住（在内存），不能持久化，
 * 也不能应答主库：
 * #1-#3为已经持久化和应答的事务日志
 * #5-#9为已经收到但却不能持久化和应答的事务日志
 * #4为未收到的事务日志。
 *
 * 顺序投票策略对于主库的负面影响比较严重：出于性能提升的原因，数据库的多版本并发控制（MVCC）使得不存在相互关联的事务得以并发处理，
 * 但上述顺序投票策略使得事务#5-#9可能被毫不相干的事务#4阻塞，且必须hold在内存。
 * 顺序投票策略对于多表事务的影响很大：
 * 设想一个事务涉及到三张表A，B，C，其中一个事务被顺序投票策略阻塞了，那么表A、B、C上的其他单表和多表事务都会被阻塞，
 * 假如A又跟表C、表D有多表事务，B和表E、表F有多表事务，C和表D、表G有多表事务，
 * 那么很多表上的事务，包括单表和多表的事务，都会被阻塞，形成一个链式反应。
 *
 * 顺序投票策略对于跨服务器的多表事务（分布式事务）的影响极大：
 * 由于事务日志需要在多个表相关的多台服务器之间同步，日志发送与接收之间的顺序更加得不到保证，
 * 许多的单表和多表的事务都可能被阻塞，包括链式反应的阻塞甚至循环阻塞，不仅增加事务延迟，
 * 甚至可能导致内存耗尽。顺序投票策略的另外一个负面作用是对故障恢复的影响。
 * 由于分布式一致性协议必须有多数派才能正常工作，所以一个参与者故障后，系统应该需要补上一个参与者，
 * 确保系统不会因为下一个参与者故障致使多数派协议被破坏，从而导致已经应答了客户的事务数据的丢失等非常严重的问题。
 * 由于单台服务器通常服务成千上万的表格，对每个表格分别写事务日志（redo log）会导致很低的性能，
 * 而从混合在一起的事务日志中提取部分表格的日志有不小的工作量，从而导致新的参与者的延迟。
 *
 * Raft的上述顺序投票策略，是Raft算法的基础之一，如果抛弃它，则Raft算法的正确性无法得到保证。
 *
 * 对于数据库之外的场景，上述缺陷可能没有很大的影响，但对于高峰期每秒钟处理成千上万的事务的数据库，是一个无法忽视的潜在性能和稳定性风险。
 *
 *
 * -------------------------------------------------------------------------------------------------
 * 看到这篇文章，我就产生了一个疑问，tidb的一致性协议也是基于raft,同时支持事务级别到可重复读，那么他的多表事务是如何实现的
 *
 */
public class TikvMultiRaft {
}
