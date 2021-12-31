package com.dzz.algorithm.dynamic;

import org.junit.Test;

public class SplitPalindromeTest {

    /*
     *
     * 分割回文串
     *给定一个字符串 s，将 s 分割成一些子串，使每个子串都是回文串。
     *
     *返回 s 所有可能的分割方案。
     *
     *示例:
     *
     *输入: “aab”
     *输出:
     *[
     *[“aa”,“b”],
     *[“a”,“a”,“b”]
     *]
     *
     * */
    @Test
    public void test() {
        splitPalindrome("aabbcaac");
    }

    /*
     * 我的思路：
     * 可以参考以前的最长回文字串来做
     * 如果是回文串，那么前后字符一定是相等，单个字符一定是回文串
     * 首先得到每个字符得下标
     * 比如 aabbcac
     * a [0,1,5]
     * b [2,3]
     * c [4,6]
     * 然后获取所有存在的回文区间
     * a {[0,0],[1,1],[0,1],[5,5]}
     * b {[2,2],[3,3],[2,3]}
     * c {[4,4],[6,6],[4,6]}
     * 这里做一次转换，变成
     * 0 {[0,0],[0,1]}
     * 1 {[1,1]}
     * 2 {[2,2],[2,3]}
     * 3 {[3,3]}
     * 4 {[4,4],[4,6]}
     * 5 {[5,5]}
     * 6 {[6,6]}
     * 接着从头开始遍历,就变成了回溯选择问题了
     *
     * 判断回文是比较浪费时间的，可以用dp优化，dp[ i ][ j ]表示 i 到 j 子串是否为回文串。
     * */
    public void splitPalindrome(String s) {
        int[][] dp = new int[s.length()][s.length()];
        for (int i = 0; i < s.length(); i++) {
            dp[i][i] = 1;
        }
        for (int i = s.length() - 1; i >= 0; i--) {
            for (int j = i + 1; j < s.length(); j++) {
                dp[i][j] = (dp[i + 1][j - 1] == 1) && (s.charAt(i) == s.charAt(j)) ? 1 : 0;
            }
        }
        System.out.println(dp);
    }

    /*
     *
     * 分割回文串2
     * 给定一个字符串 s，将 s 分割成一些子串，使每个子串都是回文串。
     *
     * 返回符合要求的最少分割次数。
     * */
    public int splitPalindrome2(String s) {
        return 0;
    }
}
