package com.dzz.algorithm.other;

/**
 * @author zoufeng
 * @date 2020-8-10
 * 遗传算法
 * 准备阶段
 * 在算法初始阶段，它会随机生成一组可行解，也就是第一代染色体。
 * 然后采用适应度函数分别计算每一条染色体的适应程度，
 * 并根据适应程度计算每一条染色体在下一次进化中被选中的概率(这个上面已经介绍，这里不再赘述)。
 * 进化阶段
 * 上面都是准备过程，下面正式进入“进化”过程。
 * 通过“交叉”，生成N-M条染色体；
 * 再对交叉后生成的N-M条染色体进行“变异”操作；
 * 然后使用“复制”的方式生成M条染色体；
 * 到此为止，N条染色体生成完毕！
 * 紧接着分别计算N条染色体的适应度和下次被选中的概率。
 * 这就是一次进化的过程，紧接着进行新一轮的进化。
 *
 * 每一次进化都会更优，因此理论上进化的次数越多越好，
 * 但在实际应用中往往会在结果精确度和执行效率之间寻找一个平衡点，一般有两种方式。
 * 1、限定进化次数
 * 2、限定允许范围。比如当算法进行X次进化后，一旦发现了当前的结果已经在误差范围之内了，那么就终止算法。
 *
 * 应用：
 * 算法都是用来解决实际问题的，到此为止，我想你对遗传是算法已经有了个全面的认识，
 * 下面我们就用遗传算法来解决一个实际问题——负载均衡调度问题。
 * 假设有N个任务，需要负载均衡器分配给M个服务器节点去处理。
 * 每个任务的任务长度、每台服务器节点(下面简称“节点”)的处理速度已知，
 * 请给出一种任务分配方式，使得所有任务的总处理时间最短。
 *
 *
 *
 */
public class GeneticAlgorithm {
}
