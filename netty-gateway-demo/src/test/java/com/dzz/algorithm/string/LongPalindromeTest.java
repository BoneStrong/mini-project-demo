package com.dzz.algorithm.string;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class LongPalindromeTest {

    /*
     * 最长回文字串
     *
     * 给你一个字符串 s，找到 s 中最长的回文子串。
     *

     * 示例 1：
     *
     * 输入：s = "babad"
     * 输出："bab"
     * 解释："aba" 同样是符合题意的答案。
     *
     * 示例 2：
     *
     * 输入：s = "cbbd"
     * 输出："bb"
     * 示例 3：
     *
     * 输入：s = "a"
     * 输出："a"
     * 示例 4：
     *
     * 输入：s = "ac"
     * 输出："a"
     *
     *
     * 思路：
     * 判断一个字符串是不是回文很简单，双指针，或者栈判断都可以
     *
     * 回文的特点是两端的字符一致，那么我们可以先找到相同字符串的区间比如babad
     * 解析后是这么几个区间
     * b [0,2]
     * a [1,3]
     * d [4]
     * 要找最长的字串，可以先从最长的区间来判断是否合法，然后再判断短的区间
     *
     * */
    public String longSubPalindrome(String s) {
        char[] chars = s.toCharArray();
        Map<Character, List<Integer>> map = new HashMap<>();
        for (int i = 0; i < chars.length; i++) {
            List<Integer> list = map.computeIfAbsent(chars[i], k -> new ArrayList<>());
            list.add(i);
        }
        AtomicReference<String> result = new AtomicReference<>(chars[0] + "");
        map.forEach((k, list) -> {
            if (list.size() > 1 && list.get(list.size() - 1) - list.get(0) + 1 > result.get().length()) {
                result.set(maxLen(chars, result.get(), list));
            }
        });
        return result.get();
    }

    public String maxLen(char[] chars, String result, List<Integer> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = i + 1; j < list.size(); j++) {
                if (i < j
                        && (list.get(j) - list.get(i)) + 1 > result.length()
                        && isP(chars, list.get(i), list.get(j))) {

                    result = new String(chars).substring(list.get(i), list.get(j) + 1);
                }
            }
        }
        return result;
    }

    private boolean isP(char[] chars, int start, int end) {
        while (start < end) {
            if (chars[start] != chars[end]) return false;
            start++;
            end--;
        }
        return true;
    }


    @Test
    public void test() {
        System.out.println(longSubPalindrome("babad"));
        System.out.println(longSubPalindrome("ac"));
    }

    /*
    * 思路二：最长公共字串
    * 根据回文串的定义，正着和反着读一样，那我们是不是把原来的字符串倒置了，然后找最长的公共子串就可以了。
    * 例如 S = "caba" ，S = "abac"，最长公共子串是 "aba"，所以原字符串的最长回文串就是 "aba"
    *
    *
    *
    * */
    public String longSubPalindrome2(String s) {
        char[] chars = s.toCharArray();
        Map<Character, List<Integer>> map = new HashMap<>();
        for (int i = 0; i < chars.length; i++) {
            List<Integer> list = map.computeIfAbsent(chars[i], k -> new ArrayList<>());
            list.add(i);
        }
        AtomicReference<String> result = new AtomicReference<>(chars[0] + "");
        map.forEach((k, list) -> {
            if (list.size() > 1 && list.get(list.size() - 1) - list.get(0) + 1 > result.get().length()) {
                result.set(maxLen(chars, result.get(), list));
            }
        });
        return result.get();
    }
}
