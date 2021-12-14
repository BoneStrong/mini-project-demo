package com.dzz.algorithm.array;

import org.junit.Test;

import java.util.LinkedList;

public class KNumSubTest {

    /*
     *
     * 题目： 和至少为 K 的最短子数组
     *  返回 A 的最短的非空连续子数组的长度，该子数组的和至少为 K 。
     *
     * 如果没有和至少为 K 的非空子数组，返回 -1 。
     *
     * 示例 1：
     *
     * 输入：A = [1], K = 1
     * 输出：1
     * 示例 2：
     *
     * 输入：A = [1,2], K = 4
     * 输出：-1
     * 示例 3：
     *
     * 输入：A = [2,-1,2], K = 3
     * 输出：3
     *
     * 解析：
     * 这个题目是求连续子数组，所以自然可以想到前缀和，也就是用一个数组统计到第i个位置的前缀和。
     * 假设arr[]的前缀和数组是preSum[]
     * 要求区间[i,j]的和就是 preSum[j]-preSum[i]
     *
     * 所以问题就变成了
     * j>i && preSum[j] - preSum[i] >= k && (j-i) 最小
     *
     *
     * 可以通俗的理解，【就像排队，我需要找到前面比我矮最少k的人，而且想让我和这个人的距离最近】；
     *
     * 双端递增队列：
     * 对于当前的“我”来说，if前面的人比我高，那我的身高减去前面高的人，值肯定为负数，那就可以直接弹走了，
     * (那可能会有个问题，这就直接弹走了？
     * 你想想，这不正好吗？首先刚进去的这个肯定更小，if弹走的满足，那这个更满足了，其次，这个离得也更近啊，所以可以大胆的弹)，
     *
     * 因为位置 sum[n] - sum[i] > sum[n] - sum[i - 1]，
     * 而且就长度来说的话 [i, n] 比 [i - 1, n] 更短，因此我们可以把 i - 1 这个位置踢出队列。
     *
     *
     * 那现在队列里剩下的都是比我矮的人了，if第一个和我的身高差值小于k，那后面就更小于k了；
     * if队首和我的身高相比差值大于k，那就可以去当做一个答案记录了，并且可以把这个值弹出，然后比较新的队首了，
     * (为什么可以弹走呢？因为后来的无论和队首比满不满足，那都没用，因为肯定我离之前那个同学更近啊);
     *
     *
     * 假如说我们当前考虑位置 i，这时区间 [0, i] 的值比区间 [0, i - 1] 的还要小，
     * 那么对于后面的位置，我们其实就不需要考虑了 i - 1 了，

     *
     * 换句话说，也就是如果数组里面都是正数，那么长度越长，区间和肯定越大，则 sums[i] 一定大于所有双向队列中的区间和，
     * 但由于可能存在负数，从而使得长度变长，区间总和反而减少了，
     * 所以如果出现上面这种情况直接将 i - 1 这个位置踢出队列即可。
     *
     * [2,-1,2]
     * [2,1,3]
     *
     * */
    @Test
    public void test() {
        System.out.println(kSum(new int[]{2, -1, 2}, 3));
    }

    private int kSum(int[] arr, int k) {

        //最小连续区间的长度
        int res = arr.length + 1;
        //获取前缀和
        int[] pre = new int[arr.length + 1];
        for (int i = 1; i < pre.length; i++) {
            pre[i] = arr[i - 1] + pre[i - 1];
        }

        //双端递增队列
        LinkedList<Integer> dequeue = new LinkedList<>();

        for (int i = 0; i < arr.length + 1; i++) {
            //保持队列单调，保证了队列里的元素都比当前的元素小
            while (!dequeue.isEmpty() && pre[i] < pre[dequeue.getLast()])
                dequeue.removeLast();//

            //弹出满足了条件的队首，逐步找到最近的；
            while (!dequeue.isEmpty() && pre[i] - pre[dequeue.getFirst()] >= k) {
                res = Math.min(res, i - dequeue.removeFirst());
            }

            //放入队尾，前面的元素都比他小
            dequeue.add(i);
        }


        return res == arr.length + 1 ? -1 : res;
    }

}
