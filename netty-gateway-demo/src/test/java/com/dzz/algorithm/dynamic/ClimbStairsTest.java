package com.dzz.algorithm.dynamic;

import org.junit.Test;

public class ClimbStairsTest {

    /*
     *
     * 70.爬楼梯
     * 描述
     *
     * 假设你正在爬楼梯。需要 n 步你才能到达楼顶。
     * 每次你可以爬 1 或 2 个台阶。你有多少种不同的方法可以爬到楼顶呢？
     * 注意：给定 n 是一个正整数。
     *
     * 示例1
     *
     * 输入： 2
     * 输出： 2
     * 解释： 有两种方法可以爬到楼顶。
     *
     * 1 步 + 1 步
     * 2 步
     * 示例2
     *
     * 输入： 3
     * 输出： 3
     * 解释： 有三种方法可以爬到楼顶。
     *
     * 1 步 + 1 步 + 1 步
     * 1 步 + 2 步
     * 2 步 + 1 步
     *
     * */
    @Test
    public void test() {
        System.out.println(climbStairs(3));
    }

    /*
     * 思路：
     * 类似凑零钱的问题
     * 不过更简单了，只有一步和两步的选择
     * dp[n]=dp[n-1]+dp[n-2]
     * */
    public int climbStairs(int n) {
        int[] dp = new int[n + 1];
        dp[0] = 1;
        dp[1] = 1;
        for (int i = 2; i < n + 1; i++) {
            dp[i] = dp[i - 1] + dp[i - 2];
        }
        return dp[n];
    }
}
