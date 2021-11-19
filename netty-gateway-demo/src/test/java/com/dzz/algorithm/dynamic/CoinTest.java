package com.dzz.algorithm.dynamic;

import org.junit.Test;

import java.util.Arrays;

public class CoinTest {

    /*
     * 凑零钱问题一
     *
     * 有面值{1，2，5，10，50，100}的硬币
     * 给定一个金额k，求最少需要几个硬币刚好凑整k
     *
     * 1. 动态规划进行暴力求解
     *
     * 假设dp[n]是凑出金额n的最小硬币数
     * 可以通过dp[n-coin]+1得到
     *
     * 那么有状态转移方程
     * dp[n]=min(dp[n-coin]+1,coin∈{1，2，5，10，50，100})
     *
     * 初始值dp[0]=0
     *
     * 2. 贪心算法
     * 保证贪心算法有效性是证明局部最优能保证全局最优
     *
     * 假如这里的金额具有一些特征，比如金额设置为1，2,5，10
     * 这个特征就是 1+2<5 ,1+2+5<10
     *
     * 分析：
     * 由于1+c1+c2+c3+...ck-1=ck - 1<ck,
     * 故当n大于ck时，
     * 可以分解为ck与n-ck的值，其中ck只用一个硬币值为ck的硬币就能得到最少硬币数，
     * 而子问题变成n-ck的最少硬币数，依次类推，贪心算法总能得到最好的结果。
     *
     * 同理，要是币值满足后者是前者的2倍或以上，也满足1+c1+c2+c3+...ck-1=ck - 1<ck。
     * 就可以使用贪心算法。
     *
     *分析——要分析什么情况下贪心算法无效，如果出现一组硬币25，6，5，1.由于1+5=6，
     * 当遇到10元时，按照贪心算法将分解为6+4*1，而其实为2*5.
     *
     *
     * */
    int[] m = new int[]{1, 2, 5, 10, 50, 100};

    @Test
    public void coinDpTest() {
        int[] dp = new int[101];
        System.out.println(dp(56, dp));
        System.out.println(tanXin(56));
    }

    /*
     * 这里动态规划
     * 状态方程依赖前面计算结果，备忘录一次遍历剪支优化
     * */
    public int dp(int target, int[] dp) {
        if (target == 0) return 0;
        for (int i = 1; i <= target; i++) {
            dp[i] = Integer.MAX_VALUE;
            for (int k : m) {
                if (i >= k) {
                    dp[i] = Math.min(dp[i], dp[i - k] + 1);
                }
            }
//            System.out.printf("dp[%s] is %s \n", i, dp[i]);
        }

        return dp[target];
    }


    public int tanXin(int target) {
        int size = 0;
        for (int i = m.length - 1; i >= 0; i--) {
            while (target >= m[i]) {
                target = target - m[i];
                size++;
            }
        }
        return size;
    }

    /*
     * Coin Change2
     * 给定一系列的coins，和一个target，现在需要使用这些coins组成target，问有多少种不同的组成方法？
     *
     *示例1：
     *输入：Coins = [1,2,5], target = 5
     *输出：4
     *
     *示例2：
     *输入：Coins = [2], target = 3
     *输出：0
     *
     *
     * 分析：
     * 此问题咋一看像是k数之后，仔细一看有些许区别
     * 【区别在于这里取数是不受限制的，那么就和传统的背包问题从前i个数取j个不同】
     * 准确来说和凑零钱很像，
     * 不过这里就不是求最小方案。
     *
     * 设目标为n的方案数是dp[n]
     * 如果n>coin, dp[n]+=dp[n-coin]
     * 直觉让人写出状态方程
     * dp[n]=Sum(dp[n-coin],coin∈Coins)
     * 但计算可以发现，这个方程是错的。
     * 比如dp[5]=dp[4]+dp[3]+dp[0]
     * 部分子集被重复计算了,dp[4]其实包含了dp[3]+1的方案
     *
     *
     *
     * 那么如何按照前面方程去计算呢，可以再次分析下
     * 比如我要凑5块钱
     * 可以先用最小的币值凑 1 1 1 1 1  这里dp[1]...dp[5]=1
     * 然后用上两块的来凑 1 1 1 2  这里其实变相的是dp[3]+2
     *  在此之前dp[3]也是只有1块凑出来的，这里也可以用上两块的来凑
     *  dp[3]=dp[1]+2
     * 接着是5快 5               也就是dp[0]+5
     *
     * 按照币值来逐步计算的话可以避免子集重复计算
     *
     *
     * */
    private int coinChange2(int target, int[] coins) {
        int[] dp = new int[target + 1];
        dp[0] = 1;
        //先从小币值开始凑
        for (int coin : coins) {
            for (int i = coin; i <= target; i++) {
                //加上新币值的方案
                dp[i] += dp[i - coin];
//                System.out.printf("dp[%s] is %s \n", i, dp[i]);
            }

        }
        return dp[target];
    }

    /*
     * dp[i][j]代表用前i种硬币凑出目标j的方案数
     * 如果j>c[i],dp[i][j]=dp[i-1][j-c[i]] 表示dp[i][j]可以用没有第i种硬币的方案数
     * 也可以不要第i种硬币就直接凑出j，所以还要加上dp[i-1][j]的方案数
     *
     * 默认不用硬币凑出目标金额的方案数为1
     * dp[0][0]=1
     *
     * */
    private int coinChange2_2(int target, int[] coins) {

        int[][] dp = new int[coins.length + 1][target + 1];
        //金额为0时，可以都不选，方案都是1
        dp[0][0] = 1;
        for (int i = 1; i <= coins.length; i++) {
            dp[i][0] = 1;
            for (int j = 1; j <= target; j++) {
                //同时，如果这次用了，那么还要加上dp[i][j - coins[i]]。
                // 这里必须强调一下，不是加上dp[i-1][j - coins[i]]，
                // 为什么呢？因为硬币可以用无数次，你用了coins[i]之后，相当于现在需要使用[0 ... i]这些硬币去凑出 j - coins[i]。
                // 如果题目规定每个硬币最多用一次，那这里就是加上dp[i-1][j - coins[i]]
                //dp[i][j-coin[i]]包含了dp[i-1][j-coin[i]]
                if (j >= coins[i - 1])
                    dp[i][j] += dp[i][j - coins[i - 1]];
                dp[i][j] += dp[i - 1][j];
            }
        }

        return dp[coins.length][target];
    }


    @Test
    public void testCoinChange2() {
        System.out.println(coinChange2(5, new int[]{1, 2, 5}));
        System.out.println(coinChange2_2(5, new int[]{1, 2, 5}));

        System.out.println("==============");
        System.out.println(coinChange2(3, new int[]{2}));
    }
    /*
     * 凑零钱问题二
     *
     * 问题一的前提是每种币值是无限的，那假如每种币值是有限的，如何求最小硬币数呢
     *
     * 引申出问题三，发工资问题
     * 再加上一个限制，比如有3个人，需要3种总额，比如56，48，73，如果求最小硬币数呢
     *
     *
     * */


    /*
     * 凑零钱变种问题  目标和
     *
     * 给一个数组（元素非负），和一个目标值，要求给数组中每个数字前添加正号或负号所组成的表达式结果与目标值S相等，
     * 求有多少种情况。
     *
     * 示例1：
     *输入：nums = [1,1,1,1,1], target = 3
     *输出：5
     *
     * 分析：假设所有元素和为sum，所有添加正号的元素的和为A，所有添加负号的元素和为B，
     * 则有sum = A + B 且 S = A - B，解方程得A = (sum + S)/2。
     * 即题目转换成：从数组中选取一些元素使和恰好为(sum + S) / 2。
     *
     * 这就是变种的凑零钱问题，从一堆零钱中找出凑出目标值的方案
     * 不过不一样的是这里的硬币每个只有一个
     * dp[i][j]表示取前i个硬币凑出目标值j的方案数
     *
     * dp[i][j]=dp[i-1][j]+(j>coin[i]?dp[i-1][j-coin[i]]:0)
     * 初始值
     * dp[0][0]=1
     *
     * */
    @Test
    public void testTargetAdd() {
        int[] m = new int[]{1, 1, 1, 1, 1};
        int target = 3;
        int t = (Arrays.stream(m).sum() + target) / 2;
        System.out.println(targetAdd(m, t));
    }

    private int targetAdd(int[] coin, int target) {
        int[][] dp = new int[coin.length + 1][target + 1];
        dp[0][0] = 1;
        for (int i = 1; i <= coin.length; i++) {
            dp[i][0] = 1;
            for (int j = 1; j <= target; j++) {
                dp[i][j] = dp[i - 1][j] + (j >= coin[i - 1] ? dp[i - 1][j - coin[i - 1]] : 0);
            }
        }
        return dp[coin.length][target];
    }
}
