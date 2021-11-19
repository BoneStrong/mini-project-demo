package com.dzz.algorithm.dynamic;

import org.junit.Test;

public class KNumTest {

    /*
     * 给定 n 个不同的正整数，整数 k（k <= n）以及一个目标数字 target。
     *在这 n 个数里面找出 k 个数，使得这 k 个数的和等于目标数字，求问有多少种方案？
     *
     *样例1
     *
     *输入:
     *List = [1,2,3,4]
     *k = 2
     *target = 5
     *输出: 2
     *说明: 1 + 4 = 2 + 3 = 5
     *样例2
     *
     *输入:
     *List = [1,2,3,4,5]
     *k = 3
     *target = 6
     *输出: 1
     *说明: 只有这一种方案。 1 + 2 + 3 = 6
     *
     *
     *输入测试数据 (每行一个参数)如何理解测试数据？
     * */
    @Test
    public void kNumTest() {
        System.out.println(kNum(new int[]{1, 2, 3, 4, 5, 6, 7, 8}, 4, 13));
        System.out.println(kNum(new int[]{1, 2, 3, 4}, 2, 5));
    }

    /*
     *如果没有k的约束。我们可以发现，这题就可以转化为背包问题。利用n个正整数达到目标target。
     * 那么有了k的约束之后，我们需要用额外的一维来维护使用的数字。
     * 所以约定状态如下，用dp[i][j][k]表示前i个数里选j个和为t的方案数。
     *
     * 假定dp[i][j][t]之前的方案数都已知，考虑dp[i][j][t]的情况。
     * dp[i][j][t]可以由dp[i−1][j−1][t−A[i−1]]的状态取A[i-1]得到。
     * dp[i][j][t]也可以由dp[i−1][j][t]直接得到，即不取A[i-1]。
     * 最后返回f[n][k][target]即可。
     *
     * dp[i][j][t] = dp[i-1][j-1][t−A[i−1]] + dp[i-1][j][t]
     *
     * */
    private int kNum(int[] arr, int k, int target) {
        int n = arr.length;
        int[][][] f = new int[n + 1][k + 1][target + 1];
        //初始化数组，不取任何数目标和为0的方案都为1
        for (int i = 0; i < n + 1; i++) {
            f[i][0][0] = 1;
        }

        for (int i = 1; i < n + 1; i++) {
            for (int j = 1; j < k + 1 && j <= i; j++) {
                for (int t = 1; t < target + 1; t++) {
                    if (t >= arr[i - 1]) {
                        //如果当前目标值t - arr[i - 1] >= 0,说明结果是可以加上之前[i - 1][j - 1][t - arr[i - 1]]的方案取t-arr[i-1]得到
                        f[i][j][t] = f[i - 1][j - 1][t - arr[i - 1]];
                    }
                    //最终存在两种可能，一种是加上之前t - A[i - 1]的方案种数，一种是之前dp[i - 1][j][t]的方案种数
                    f[i][j][t] += f[i - 1][j][t];
                } // for t
            } // for j
        } // for i
        return f[n][k][target];
    }

}
