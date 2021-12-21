package com.dzz.algorithm.greedy;

import org.junit.Test;

public class HeaterTest {

    /*
     * 冬季已经来临。你的任务是设计一个有固定加热半径的供暖器向所有房屋供暖。
     * 在加热器的加热半径范围内的每个房屋都可以获得供暖。
     * 现在，给出位于一条水平线上的房屋houses 和供暖器heaters 的位置，请你找出并返回可以覆盖所有房屋的最小加热半径。
     *
     * 说明：所有供暖器都遵循你的半径标准，加热的半径也一样。
     *

     * 示例 1:
     *
     * 输入: houses = [1,2,3], heaters = [2]
     * 输出: 1
     * 解释: 仅在位置2上有一个供暖器。如果我们将加热半径设为1，那么所有房屋就都能得到供暖。
     * 示例 2:
     *
     * 输入: houses = [1,2,3,4], heaters = [1,4]
     * 输出: 1
     * 解释: 在位置1, 4上有两个供暖器。我们需要将加热半径设为1，这样所有房屋就都能得到供暖。
     * 示例 3：
     *
     * 输入：houses = [1,5], heaters = [2]
     * 输出：3
     *
     *
     * */
    @Test
    public void test() {
        System.out.println(heaters(new int[]{1, 2, 3}, new int[]{2}));
        System.out.println(heaters(new int[]{1, 2, 3, 4}, new int[]{1, 4}));
        System.out.println(heaters(new int[]{1, 5}, new int[]{2}));
    }

    /*
     *
     * 要想覆盖全部房屋，
     * 只需要找到覆盖点与房屋需要的最大距离就行
     *
     * 覆盖点与覆盖点之间需要的最大距离是距离的1半
     * 覆盖点如果不是两端的点，那么需要覆盖的最大距离是覆盖点到端点的距离
     *
     * */
    public int heaters(int[] arr, int[] heaters) {
        int temp = 1;
        int max = 0;
        if (heaters[0] != 1)
            temp = 2 - heaters[0];
        for (int i = 1; i < heaters.length - 1; i++) {
            max = Math.max(max, (heaters[i] - temp) / 2);
            temp = heaters[i];
        }
        if (heaters[heaters.length - 1] != arr[arr.length - 1]) {
            max = Math.max(max, (arr[arr.length - 1] - heaters[heaters.length - 1]));
            if (heaters.length > 1) {
                max = Math.max(max, (heaters[heaters.length - 1] - heaters[heaters.length - 2]) / 2);
            }
        }else {
            if (heaters.length > 1) {
                max = Math.max(max, (heaters[heaters.length - 1] - temp) / 2);
            }
        }
        return max;
    }
}
