package com.dzz.algorithm.string;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class KNumSubString {

    /*
     * 至少有K个重复字符的最长子串 要求O(nlogn)时间复杂度,O(1)空间复杂度
     * 重排链表为有序链表：148. 排序链表，其实就是数归并排序的链表实现，这里就展开了。
     *
     * 题目
     * 给你一个字符串 s 和一个整数 k ，请你找出 s 中的最长子串，
     *  要求该子串中的每一字符出现次数都不少于 k。返回这一子串的长度
     *
     * 示例 1:
     * 输入:s = "aaabb", k = 3
     * 输出:3
     * 最长子串为 "aaa" ，其中 'a' 重复了 3 次。
     *
     * 示例 2:
     * 输入:s = "ababbc", k = 2
     * 输出:5
     * 最长子串为 "ababb" ，其中 'a' 重复了 2 次， 'b' 重复了 3 次
     *
     * */


    /*
     * 先不考虑任何优化来处理这个问题。
     * 用直观的方式解题：比如 ababbc 2
     * 1. 获取所有字符的下标数组
     * - Ia [0,2]
     * - Ib [1,3,4]
     * - Ic [5]
     *
     * 其中 Ic是安全下标，因为它出现的次数少于2。任何区间包含该下标的字符都是不符合题意的，
     * 这样问题就可以分解成区间问题
     * 寻找 [0,4]这些区间内字串最长的问题
     *
     * */
    private int kNumSubString_normal(String s, int k) {
        if (s.length() == 1) return 1;

        Map<Character, Integer> map = new HashMap<>();
        char[] chars = s.toCharArray();
        for (int i = 0; i < s.length(); i++) {
            map.merge(chars[i], 1, Integer::sum);
        }
        int mark = 0;
        int len = 1;
        for (int i = 0; i < s.length(); i++) {
            if (i > mark && map.get(chars[i]) < k) {
                len = Math.max(len, kNumSubString_normal(s.substring(mark, i), k));
                mark = i + 1;
            }
        }
        if (mark == 0)
            len = s.length();
        return len;
    }

    @Test
    public void test() {
        System.out.println(kNumSubString_normal("aaabb", 3));
        System.out.println(kNumSubString_normal("ababbc", 2));
    }

    /*
     * 求最少k重复字串的问题还算简单。
     *
     * 那么如果问题变成最多呢，问题复杂度一下就提高了很多
     *
     * */
}
