package com.dzz.algorithm.dynamic;

import org.junit.Test;

public class DifferentRoadTest {


    /*
     * 不同路径
     *
     * 一个机器人位于一个 m x n 网格的左上角 （起始点在下图中标记为 “Start” ）。
     * 机器人每次只能向下或者向右移动一步。机器人试图达到网格的右下角（在下图中标记为 “Finish” ）。
     * 问总共有多少条不同的路径？
     *
     * 示例1：
     * 输入：m=3,n=7
     * 输出：28
     *
     * 示例2：
     * 输入：m=3,n=2
     * 输出：3
     * 从左上角开始，有3条路到右下角
     * 右-下-下
     * 下-下-右
     * 下-右-下
     *
     * */
    @Test
    public void test() {
        System.out.println(differentRoads(3, 2));//3
        System.out.println(differentRoads(3, 7));//28
        System.out.println(differentRoads(3, 3));//6
    }

    /*
     * 思路：
     * 反向思考，由于只能向右和向下移动，到达右下角目的地的前一步只有上面和左边
     * dp[m][n]定义为到达第m行,第n列的方案数
     * 这样很容易推出状态转移方程
     * dp[m][n]=dp[m-1][n]+dp[m][n-1]
     *
     * 初始化状态:
     *  dp[m][0]=1
     *  dp[0][n]=1
     *
     *
     * */
    public int differentRoads(int m, int n) {
        int[][] dp = new int[m + 1][n + 1];
        for (int i = 1; i < m + 1; i++) {
            for (int j = 1; j < n + 1; j++) {
                if (i == 1 || j == 1) {
                    dp[i][j] = 1;
                } else {
                    dp[i][j] = dp[i - 1][j] + dp[i][j - 1];
                }
            }
        }
        return dp[m][n];
    }

}
