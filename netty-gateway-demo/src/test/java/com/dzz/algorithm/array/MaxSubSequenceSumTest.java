package com.dzz.algorithm.array;

import org.junit.Test;

public class MaxSubSequenceSumTest {
    /*
     *
     * 给定一个整数数组 nums ，找到一个具有最大和的连续子数组（子数组最少包含一个元素），返回其最大和。
     *
     * 示例:
     *
     * 输入: [-2,1,-3,4,-1,2,1,-5,4],
     * 输出: 6
     * 解释: 连续子数组 [4,-1,2,1] 的和最大，为 6。
     *
     * 进阶:
     * 如果你已经实现复杂度为 O(n) 的解法，尝试使用更为精妙的分治法求解。
     *
     *
     * 解析：
     * 这个只要求最大和，那么保证最大的话基本就是累加更多的正数，尽量避开负数
     * 原数据可以相邻的正数和正数累加，负数和负数先累加
     * a,-b,c,-d,e,-f ，这样数据流就很好计算
     * 假设第一个是负数，直接下个正数开始计算
     * 接着就是 a,-b,c这种情况
     * 如果b>a或者c任何一个,三者相加就是负收益，只能取max(a,c)中的一个保证收益最大
     * 相反只要 b< min(a,c)，三者相加的正收益最高
     *
     * */
    @Test
    public void test() {
//        System.out.println(maxSubSequenceSum(new int[]{-2, 1, 4, 5, 1, -3, 4, -1, -2, 2, 1, 1, -5, -3, 4}));
//        System.out.println(maxSubSequenceSum_dp(new int[]{-2, 1, 4, 5, 1, -3, 4, -1, -2, 2, 1, 1, -5, -3, 4}));
        System.out.println(maxSubSequenceSum_greedy(new int[]{-2, 1, 4, 5, 1, -3, 4, -1, -2, 2, 1, 1, -5, -3, 4}));
    }

    private int maxSubSequenceSum(int[] arr) {

        int index = 0;
        boolean positive = true;//正数
        int temp = 0;
        //整理arr,先将数据变成正负间隔的
        for (int i = 0; i < arr.length; i++) {
            if ((positive && (arr[i] < 0)) || (!positive && arr[i] > 0)) {
                positive = !positive;
                if (index != i) {
                    arr[index] = temp;
                    temp = 0;
                    index++;
                }
            }
            temp += arr[i];
        }
        arr[index] = temp;

        int maxSum = 0;
        temp = 0;
        for (int i = 0; i <= index; i++) {
            //负收益直接跳过
            if (arr[i] < 0 && arr[i] + temp < 0) {
                temp = 0;
                continue;
            }
            //正收益,则判断是否比最大值大
            temp += arr[i];
            maxSum = Math.max(temp, maxSum);
        }

        return maxSum;
    }


    /*
     * 动态规划解析
     *
     * 假设新元素加进来的时有一个【右边的最大贡献度】
     * 那第一元素的最大贡献度就是本身
     * 第i个加进来时的贡献度依赖他前面最右边的贡献度
     * 如果前面右边的贡献度小于0，那么比较他和前面的最大和值比较，得到连续和最值
     * 如果前面的最右贡献度大于0，那么加上这个元素的贡献度去和最值比较，得到连续和最值
     *
     * */
    public int maxSubSequenceSum_dp(int[] arr) {
        int dp = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (arr[i - 1] > 0) arr[i] += arr[i - 1];
            dp = Math.max(arr[i], dp);
        }
        return dp;
    }


    /*
     * 贪心解析
     *
     * 每加一个新元素时，判断他+前面得最右贡献度，是否比他本身贡献度大
     * ，如果比和前面相加的大
     *
     *
     * */
    public int maxSubSequenceSum_greedy(int[] arr) {
        int ans = arr[0];
        int maxAns = arr[0];
        for (int i = 1; i < arr.length; i++) {
            ans = Math.max(ans + arr[i], arr[i]);
            maxAns = Math.max(maxAns, ans);
        }
        return maxAns;
    }
}
