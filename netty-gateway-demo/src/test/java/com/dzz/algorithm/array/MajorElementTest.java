package com.dzz.algorithm.array;

import org.junit.Test;

public class MajorElementTest {

    /*
     *
     * 题目：多数元素
     *  给定一个大小为 n 的数组，找到其中的多数元素。多数元素是指在数组中出现次数 大于 ⌊ n/2 ⌋ 的元素。
     * 你可以假设数组是非空的，并且给定的数组总是存在多数元素。
     *
     * 示例：
     * 输入：[3,2,3]
     * 输出：3
     * 输入：[2,2,1,1,1,2,2]
     * 输出：2
     *
     *
     *
     * 解析：
     * 排序法，在n/2的位置的数就是多数元素
     * 计数法，hash或者位对元素计数，最大的就是多数元素
     * 双指针消息乐，两个数字不同两两消除，剩下的一定是多数元素
     * 分治法，把数组分两份，每份都有多数元素，多数元素的一定是其中一个且次数最多
     *
     *
     * 这里打算使用消消乐法
     *
     *
     * */
    @Test
    public void test() {
        System.out.println(majorityElement(new int[]{2, 2, 1, 1, 1, 2, 2}));
        System.out.println(majorityElement(new int[]{3, 2, 3}));

    }

    private int majorityElement(int[] arr) {
        int i = 0;
        int j = arr.length - 1;
        while (i < j) {
            if (arr[i] != arr[j]) {
                i++;
                j--;
            } else {
                int temp = i;
                while (arr[temp] == arr[j] && temp < j) {
                    temp++;
                }
                int t = arr[temp];
                arr[temp] = arr[j];
                arr[j] = t;
            }
        }
        return arr[i];
    }
}
