package com.dzz.algorithm.string;

public class LongSameSubStringTest {

    /*
     * 最长公共字串
     *
     * 有两个字符串（可能包含空格）,请找出其中最长的公共连续子串,输出其长度。(长度在1000以内)
     *
     * 例如：
     * 输入：abcde bcd
     * 输出：3
     *
     *
     *
     * 解析：
     * str=acbcbcef，str2=abcbced，则str和str2的最长公共子串为bcbce，最长公共子串长度为5
     *
     * 1、把两个字符串分别以行和列组成一个二维矩阵。
     * 2、比较二维矩阵中每个点对应行列字符中否相等，相等的话值设置为1，否则设置为0。
     * 3、通过查找出值为1的最长对角线就能找到最长公共子串。
     *
     *  当A[i] != B[j]，dp[i][j] = 0
     * 否则 dp[i][j] = dp[i - 1][j - 1] + 1
     * 全部都归结为一个公式即可，二维数组默认值为0
     *
     * */
}
