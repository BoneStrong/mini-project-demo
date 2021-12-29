package com.dzz.algorithm.monotonicity;

import org.junit.Test;

import java.util.Stack;

public class GoodWorkTimeTest {

    /*
     *
     * 表现良好的最长时间段
     *  给你一份工作时间表 hours，上面记录着某一位员工每天的工作小时数。
     * 我们认为当员工一天中的工作小时数大于 8 小时的时候，那么这一天就是「劳累的一天」。
     * 所谓「表现良好的时间段」，意味在这段时间内，「劳累的天数」是严格 大于「不劳累的天数」。
     * 请你返回「表现良好时间段」的最大长度。
     *
     * 示例 1：
     * 输入：hours = [9,9,6,0,6,6,9]
     * 输出：3
     * 解释：最长的表现良好时间段是 [9,9,6]。
     * 1
     * 2
     * 3
     * 提示：
     *
     * 1 <= hours.length <= 10000
     * 0 <= hours[i] <= 16
     *
     * */
    @Test
    public void test() {
        System.out.println(goodWorkTime(new int[]{9, 9, 6, 0, 6, 6, 9}));//3
        System.out.println(longSlope(new int[]{6, 0, 8, 2, 1, 5}));//3
    }

    /*
     *
     * 思路：
     * 如果是表现良好的时间段，劳累的天数>不劳累的天数
     * 那么区间[i,j]的前缀和pre[j]-pre[i]>=0;
     *
     * 那么这道题的关键就是找到最远比i大的j
     * 【根据以往的经验，单调栈可以快速求最近的元素
     * 那么如果求最远的元素，可以单调栈找到最近的元素后继续找这个元素的最近】
     *
     * 要求区间内最大的差值呢？比如一个数组A[6,0,8,2,1,5],要求出A[i]<A[j]的最长区间，其中i<j
     * 按照贪心的原则就是尽量找到最小的左边界和找到最大的右边界下标
     * 假设确认了右边界R，有两个左边界下标 L1<L2,如果其中L2是最优的左边界，那么肯定v[L2]<v[L1]
     * 因为如果v[L1]<v[L2],那么L1才是最小的左边界
     *
     * 这样找左边界的问题其实转化为找最小值问题
     * 从左到右遍历，构建单调递减栈，因为是往栈顶插值，栈顶的值是最小值，入栈的元素是可能的左边界
     *
     *
     * 为什么要从后往前找右边界呢？
     *
     * 也是因为贪心，我们要使 j 越大越好。
     * 因为从后往前找到的第一个右边界 j 才是最靠右的。
     * 如果从前面往后面找右边界，即使找到了 sum[j] > sum[i]，
     * 我们也无法确定 j 就是此时表现良好的时间段的右边界。
     * 因为可能后面还有一个更大的下标 y，使得 sum[y] > sum[i]。
     * 所以从后往前遍历才是最优的。
     *
     *
     * */
    public int goodWorkTime(int[] times) {
        int result = 0;
        int[] pre = new int[times.length + 1];//方便计算设p[0]=0

        Stack<Integer> stack = new Stack<>();
        stack.push(0);
        //初始化，前缀和，这里为了简化计算，将劳累一天记为1，不劳累为-1
        for (int i = 1; i < times.length; i++) {
            pre[i] = pre[i - 1] + (times[i - 1] > 8 ? 1 : -1);

            //单调递增,获取最小的左边界
            if ((stack.isEmpty()) || pre[stack.peek()] > pre[i])
                stack.push(i);
        }

        //单调递减，获取最大的右边界
        //这里面为什么边界直接pop,假设边界a<b<c, 从后往前遍历，c,b都符合条件
        // 但是既然都符合条件，c-a肯定是大于c-b的，所以a这个边界计算完直接出栈
        //同理可以反推到其他边界，这个也是贪心的思想
        for (int i = pre.length - 1; i >= 0; i--) {
            while (!stack.isEmpty() && pre[stack.peek()] < pre[i]) {
                result = Math.max(result, i - stack.pop());
            }
        }

        return result;
    }

    public int longSlope(int[] highs) {
        int result = 0;
        Stack<Integer> stack = new Stack<>();
        for (int i = 0; i < highs.length; i++) {
            if (stack.isEmpty() || highs[stack.peek()] > highs[i])
                stack.push(i);
        }

        for (int i = highs.length - 1; i >= 0; i--) {
            if (i < result + 1) break;
            while (!stack.isEmpty() && highs[i] > highs[stack.peek()]) {
                result = Math.max(result, i - stack.pop());
            }
        }
        return result + 1;
    }
}
