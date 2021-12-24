package com.dzz.algorithm.array;

import org.junit.Test;

public class EatAppleTest {

    /*
     * 有一棵特殊的苹果树，一连 n 天，每天都可以长出若干个苹果。在第 i 天，树上会长出 apples[i] 个苹果，
     * 这些苹果将会在 days[i] 天后（也就是说，第 i + days[i] 天时）腐烂，变得无法食用。也可能有那么几天，
     * 树上不会长出新的苹果，此时用 apples[i] == 0 且 days[i] == 0 表示。
     *
     * 你打算每天 最多 吃一个苹果来保证营养均衡。注意，你可以在这 n 天之后继续吃苹果。
     *
     * 给你两个长度为 n 的整数数组 days 和 apples ，返回你可以吃掉的苹果的最大数目。
     *
     * 示例 1：
     *
     * 输入：apples = [1,2,3,5,2], days = [3,2,1,4,2]
     * 输出：7
     * 解释：你可以吃掉 7 个苹果：
     * - 第一天，你吃掉第一天长出来的苹果。
     * - 第二天，你吃掉一个第二天长出来的苹果。
     * - 第三天，你吃掉一个第二天长出来的苹果。过了这一天，第三天长出来的苹果就已经腐烂了。
     * - 第四天到第七天，你吃的都是第四天长出来的苹果。
     * 示例 2：
     *
     * 输入：apples = [3,0,0,0,0,2], days = [3,0,0,0,0,2]
     * 输出：5
     * 解释：你可以吃掉 5 个苹果：
     * - 第一天到第三天，你吃的都是第一天长出来的苹果。
     * - 第四天和第五天不吃苹果。
     * - 第六天和第七天，你吃的都是第六天长出来的苹果。
     * */
    @Test
    public void test() {
        System.out.println(maxEatAppleNumbers(new int[]{1, 2, 3, 5, 2}, new int[]{3, 2, 1, 4, 2}));//7
        System.out.println(maxEatAppleNumbers(new int[]{3, 0, 0, 0, 0, 2}, new int[]{3, 0, 0, 0, 0, 2}));//5
        System.out.println(maxEatAppleNumbers(new int[]{2, 1, 10}, new int[]{2, 10, 1}));//4
    }

    /*
     * 思考：
     * 这道题目本质上就是一个区间投影的问题。
     * 比如第一天有3个苹果，这苹果只能保存2天
     * 他在日期时间轴上只能覆盖[1,1+Min(3,2)]
     *
     * 我们只要求出区间和，即得到结果
     * */
    private int maxEatAppleNumbers(int[] apples, int[] days) {
        int result = 0;
        int left = 0;
        int right = 0;//不包括
        for (int i = 0; i < apples.length; i++) {
            int temp = 0; //可以延续的天数
            if (apples[i] > 0 && days[i] == 0)
                temp = 1;
            else temp = Math.min(apples[i], days[i]);

            if (right > i) {
                right = Math.max(i + temp, right);
                continue;
            }

            if (right == i) {
                right += temp;
            } else {
                result += (right - left);
                left = i;
                right = left + temp;
            }


        }
        return result + (right - left);
    }
}
