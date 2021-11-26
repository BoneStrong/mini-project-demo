package com.dzz.algorithm.greedy;

import org.junit.Test;

public class SkipGameTest {


    /*
     * 跳跃游戏
     *
     * 描述
     * 给定一个非负整数数组 nums ，你最初位于数组的 第一个下标 。
     * 数组中的每个元素代表你在该位置可以跳跃的最大长度。
     * 判断你是否能够到达最后一个下标。
     *
     * 示例 1：
     * 输入：nums = [2,3,1,1,4]
     * 输出：true
     * 解释：可以先跳 1 步，从下标 0 到达下标 1, 然后再从下标 1 跳 3 步到达最后一个下标。
     *
     * 示例 2：
     * 输入：nums = [3,2,1,0,4]
     * 输出：false
     * 解释：无论怎样，总会到达下标为 3 的位置。但该下标的最大跳跃长度是 0 ， 所以永远不可能到达最后一个下标。
     *
     *
     * 解析：
     * 先不考虑贪心算法的话，
     * 先考虑正推模拟走法，遍历所有的可能。
     *
     * */
    private boolean skip(int[] arr, int start, int[] mark) {
        if (start == (arr.length - 1)) return true;
        if (mark[start] == 1) return false;
        mark[start] = 1;
        int i = arr[start];
        int minIndex = Math.max(start - i, 0);
        int maxIndex = Math.min(start + i, arr.length - 1);
        if (maxIndex >= arr.length - 1) return true;//提前走完

        for (int j = minIndex; j <= maxIndex; j++) {
            if (skip(arr, j, mark)) return true;
        }
        return false;
    }


    /*
     * 贪心的思路
     *
     * 比如下面是跳跃的路径
     * 0-1-2-3-4-5；
     * 假如第一步可以最大跳跃到3的位置，按照题意他也有能力跳到0，1，2的位置
     * 所以可以跳跃的位置一定是连续的，及如果可以跳跃到i，那么一定可以跳跃到i-1的位置。
     *
     * 那么这个题目用贪心的话是非常简单的。只要找到能跳到的最远位置就行了
     *
     * */
    private boolean skipGreedy(int[] arr) {
        int max = 0;
        for (int i = 0; i < arr.length - 1; i++) {
            max = Math.max(max, i + arr[i]);
        }
        return max >= arr.length - 1;
    }

    @Test
    public void skipTest() {
        System.out.println(skip(new int[]{2, 3, 1, 1, 4}, 0, new int[5]));
        System.out.println(skip(new int[]{3, 2, 1, 0, 4}, 0, new int[5]));

        System.out.println(skipGreedy(new int[]{2, 3, 1, 1, 4}));
        System.out.println(skipGreedy(new int[]{3, 2, 1, 0, 4}));
    }

}
