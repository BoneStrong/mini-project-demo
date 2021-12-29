package com.dzz.algorithm.greedy;

import org.junit.Test;

public class CutRopeTest {

    /*
     *
     * 给你一根长度为n的绳子，请把绳子剪成m段（m、n都是整数，n>1并且m>1），
     * 每段绳子的长度记为k[0]，k[1]，...，k[m]。请问k[0]*k[1]...*k[m]可能的最大乘积是多少？
     *
     * 例如：当绳子的长度为8时，我们把它剪成长度为2、3、3的三段，此时最大的乘积是18.
     *
     *
     * */
    @Test
    public void test() {

    }

    /*
     *
     * 解析：
     * 设dp[i]是长度为i是最大的乘积
     * dp[1]=1
     * dp[2]=1
     * dp[3]=2
     *
     * dp[i]=max(dp[i-j]*j),j∈（1,i-2]
     * */
    public int dpCut(int n) {
        int[] dp = new int[n + 1];
        dp[1] = 1;
        dp[2] = 1;
        dp[3] = 2;
        for (int i = 4; i < n + 1; i++) {
            for (int j = 2; j < n - 1; j++) {
                dp[i] = Math.max(dp[i], dp[i - j] * j);
            }
        }
        return dp[n];
    }

    /*
     * 解析：
     * 数学证明.先思考下减的最小单位是什么。其实是2和3.只要长度大与3的数分解后就是2和3，这个道题没有其他选择
     *
     * 当n>=5时，证明3(n-3)>2(n-2)>n
     * 因此我们应该将可能多的剪成长度为3的绳子段
     * 如果剩余为4则分成2*2
     * 剩余为5则分成2*3
     *
     * */
    public int greedyCut(int n) {
        if (n < 3) return 1;
        if (n == 3) return 2;
        int result = 1;
        while (n >= 3) {
            if (n == 4) {
                result *= 4;
            } else if (n == 5) {
                result *= 6;
            } else {
                result *= 3;
                n -= 3;
            }
        }
        return result;
    }
}
