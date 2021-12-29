package com.dzz.algorithm.string;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class ScrambleStringTest {

    /*
     *
     * 扰乱字符串
     *
     *  使用下面描述的算法可以扰乱字符串 s 得到字符串 t ：
     * - 如果字符串的长度为 1 ，算法停止
     * - 如果字符串的长度 > 1 ，执行下述步骤：
     *
     * 1. 在一个随机下标处将字符串分割成两个非空的子字符串。即，如果已知字符串 s ，则可以将其分成两个子字符串 x 和 y ，且满足 s = x + y 。
     * 2. 随机 决定是要「交换两个子字符串」还是要「保持这两个子字符串的顺序不变」。即，在执行这一步骤之后，s 可能是 s = x + y 或者 s = y + x 。
     * 3. 在x 和 y 这两个子字符串上继续从步骤 1 开始递归执行此算法。

     * 我们将 "rgtae" 称作 "great" 的一个扰乱字符串。
     *
     * 给出两个长度相等的字符串 s1 和 s2，判断 s2 是否是 s1 的扰乱字符串。
     * 示例 1:
     * 输入: s1 = "great", s2 = "rgeat"
     * 输出: true
     *
     * 示例 2:
     * 输入: s1 = "abcde", s2 = "caebd"
     * 输出: false
     * */
    @Test
    public void test() {
        System.out.println(isScramble("great", "rgeat"));
        System.out.println(isScramble("abcde", "caebd"));
    }

    /*
     *
     * 思路1：排序比对法
     * 假设是互为扰乱字符串，那么字符的数量肯定是一致的，里面字符的次数也应该是一致。
     *
     * 最简单的就是用两个容器，比如map，装下字符和字符出现的次数，比对一下就知道是不是了
     * 时间复杂度 o（3n）,空间复杂度o(2n)
     *
     * 不过看网上的题解都是递归，dp之类的，难道说字符任意排列组合的字符串不是扰乱字符串？
     *
     * 可以先证明一下：
     * 比如 12345这个字符串它有120种排列组合
     * 那么按题意的规则是否能生成120种规则呢，只要证明相等，则该思路Ok。
     *
     * testC方法证明了 按题规则生成的字符串并没有覆盖全排列。
     *
     * 所以思路一GG。
     *
     * 思路2; 暴力计算
     *
     * 可以采用思路1里面的testC获取字符串的所有干扰字符串，不过这个时间和空间复杂度都比较高。
     * 不友好。
     *
     * 还是考虑优化的思路。
     *
     * 思路三：递归
     * 递归分解字符串对比相应字串
     *
     *
     *
     * */
    private boolean scrambleString(String s1, String s2) {
        return false;
    }


    public Set<String> testC(String s) {
        Set<String> set = new HashSet<>();
        set.add(s);
        if (s.length() == 1) return set;
        for (int i = 1; i < s.length(); i++) {
            Set<String> set1 = testC(s.substring(0, i));
            Set<String> set2 = testC(s.substring(i));

            set1.forEach(s1 ->
                    set2.forEach(s2 -> {
                        set.add(s1 + s2);
                    }));

            set2.forEach(s2 ->
                    set1.forEach(s1 -> {
                        set.add(s2 + s1);
                    }));

        }
        return set;
    }


    public boolean isScramble(String s1, String s2) {
        if (s1.length() != s2.length())// 长度不等，必定不能变换
            return false;
        if (s1.equals(s2)) // 如果两个字符串相等，直接返回true
            return true;
        // 如s1和s2中字符个数不相同，直接返回false
        if (!judgeCharacter(s1, s2))
            return false;
        for (int i = 1; i < s1.length(); i++) {
            // 如果能满足原顺序相等或交换后相等直接返回 true即可
            if (isScramble(s1.substring(0, i), s2.substring(0, i))
                    &&
                    isScramble(s1.substring(i), s2.substring(i)))
                return true;
            if (isScramble(s1.substring(0, i), s2.substring(s1.length() - i))
                    &&
                    isScramble(s1.substring(i), s2.substring(0, s2.length() - i)))
                return true;
        }
        return false;
    }


    private boolean judgeCharacter(String s1, String s2) {
        return s2.chars().sum() == s1.chars().sum();
    }


    @Test
    public void testTest() {
        Set<String> set = testC("12345");
        System.out.println(set);
        System.out.println("12345".chars().sum() == "54321".chars().sum());
    }
}
