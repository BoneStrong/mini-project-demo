package com.dzz.algorithm.dynamic;

import org.junit.Test;

public class CandyTest {

    @Test
    public void test1() {
        System.out.println("1, 0, 2, 3, 1, 5");
        candyTest_3(new int[]{1, 0, 2, 3, 1, 5});
        System.out.println();
        candyTest_3_2(new int[]{1, 0, 2, 3, 1, 5});
    }


    /*
     *
     * 老师想给孩子们分发糖果，有 N 个孩子站成了一条直线，老师会根据每个孩子的表现，预先给他们评分。
     * 你需要按照以下要求，帮助老师给这些孩子分发糖果：
     * - 每个孩子至少分配到 1 个糖果。
     * - 相邻的孩子中，评分高的孩子必须获得更多的糖果。
     *那么这样下来，老师至少需要准备多少颗糖果呢？
     *
     * 示例 1:
     * 输入: [1,0,2]
     * 输出: 5
     * 解释: 你可以分别给这三个孩子分发 2、1、2 颗糖果。
     *
     * 解析：
     * 直接解法：
     * 先每人分发一个糖果，然后依次把当前孩子和前一个孩子对比。
     * 如果比前面的分高，等到的糖果肯定是比前面的多一个，递归判断后面的孩子是否满足条件
     * 反之，
     * 如果比前面的分低，等到的糖果肯定是比前面的少一个，递归判断前面的孩子是否满足条件
     *
     * */
    private void candyTest_3(int[] core) {
        int[] children = new int[core.length];
        for (int i = 0; i < core.length; i++) {
            children[i] = 1;
        }
        for (int i = 1; i < core.length; i++) {
            if (core[i] > core[i - 1]) {
                while (i < core.length && core[i] > core[i - 1] && children[i] <= children[i - 1]) {
                    children[i] = children[i - 1] + 1;
                    i++;
                }
            }
            if (i < core.length && core[i] < core[i - 1]) {
                while (i > 0 && core[i] < core[i - 1] && children[i] >= children[i - 1]) {
                    children[i - 1] = children[i] + 1;
                    i--;
                }
            }
        }

        for (int child : children) {
            System.out.print(child + ", ");
        }
    }

    /*
     * 动态规划解法：
     * 每个人分多少个糖果作为一次决策，比如给第i个孩子分配糖果，这要取决于第i-1和第i+1个孩子的分配情况，因为这取决于相邻孩子的分发情况。
     * 也就是说：假如我们知道了 i - 1 和 i + 1 个孩子分配的糖果个数，那么给第 i 个孩子分配糖果个数的问题就迎刃而解了，这可以分成两种情况：
     * 相邻的孩子中，评分高 且站在右边 的孩子必须获得更多的糖果
     * 相邻的孩子中，评分高 且站在左边 的孩子必须获得更多的糖果
     * 针对第1个问题，我们把解决 i 的问题 转换为了 i - 1 的子问题；从前到后扫描一遍即可
     * 针对第2个问题，我们把解决 i 的问题 转换为了 i + 1 的子问题,从后到前扫描一遍，此时，与前一遍扫描计算的结果相比，取最大值
     * */
    private void candyTest_3_2(int[] core) {
        int[] children = new int[core.length];
        for (int i = 0; i < core.length; i++) {
            children[i] = 1;
        }

        for (int i = 1; i < core.length; i++) {
            if (core[i] > core[i - 1])//分高的在右边
                children[i] = children[i - 1] + 1;
        }

        for (int i = core.length - 1; i > 0; i--) {
            if (core[i] < core[i - 1])//分高的在左边
                children[i - 1] = Math.max(children[i] + 1, children[i - 1]);
        }

        for (int child : children) {
            System.out.print(child + ", ");
        }
    }
}
