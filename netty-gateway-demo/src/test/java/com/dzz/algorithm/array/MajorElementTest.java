package com.dzz.algorithm.array;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;

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
                while (arr[temp] == arr[j] && temp < j) {//相等则后右边的指针交换，然后一起消消乐
                    temp++;
                }
                int t = arr[temp];
                arr[temp] = arr[j];
                arr[j] = t;
            }
        }
        return arr[i];
    }


    /*
     * 多数元素进阶（水帖王问题进阶）
     *
     * 社区有k个水贴王，他们发的贴子都超过了全部贴子的1/(k+1),求出这几个水王.
     *
     * 比如有3个水王，他们发的贴子都超过了1/4，求这3个水王
     *
     * 分析：
     * 如果按照消消乐法，则需要证明这么一个问题
     * 假设某个水王的贴子数是X，贴子总数是N，N>=K+1, X/N>1/K,
     * 则K+N<N<KX, N-K<KX-K
     *  ((X-1)/(N-K-1))> ((X-1)/(KX-K))=1/(K+1)
     *
     * 所以每次消除K+1个不同的数字，剩下的水王贴子肯定还是大于1/（1+k）的
     *
     * 但问题是：最后需要剩下多少贴子合适呢？
     * 因为如果剩下
     *
     *
     *
     * */
    private int[] majorityElementPro(int[] arr, int k) {
        HashMap<Integer, Integer> map = new HashMap<>(k + 1);
        int count = 0;
        int i = 0;
        while (i <= (arr.length - k)) {
            if (count == k + 1) {
                count = 0;
                map.clear();
                continue;
            }
            if (map.get(arr[i]) == null) {
                map.put(arr[i], i);
                count++;
                i++;
            } else {
                int temp = i + 1;

                while (temp < arr.length) {
                    if (map.get(arr[temp]) == null) {
                        map.put(arr[temp], temp);
                        count++;

                        //swap
                        int t = arr[temp];
                        arr[temp] = arr[i];
                        arr[i] = t;

                        i++;
                        break;
                    }
                    temp++;
                }
                if (temp == arr.length) break;
            }
        }

        map.clear();
        int[] result = new int[k];
        int j = 0;
        while (i < arr.length) {
            if (map.get(arr[i]) == null) {
                map.put(arr[i], i);
                result[j] = arr[i];
                j++;
            }
            i++;
        }
        return result;
    }


    @Test
    public void test2() {
        System.out.println(Arrays.toString(majorityElementPro(new int[]{2, 2, 4, 5, 1, 3, 1, 1, 3, 3, 2}, 3)));
    }
}
