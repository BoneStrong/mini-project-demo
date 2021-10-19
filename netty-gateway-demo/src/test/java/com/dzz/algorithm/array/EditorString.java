package com.dzz.algorithm.array;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class EditorString {


    /*
     *给定二个字符串s1和s2，计算将s1转化成s2的最小操作数
     *
     * 只能进行3种操作：
     * 1. 删除一个字符
     * 2. 插入一个字符
     * 3. 替换一个字符
     * */
    private int editorString(String source, String target, int i, int j) {
        if (i < 0) return j + 1; //要么是删除多余的，要么是补充不足的
        if (j < 0) return i + 1;

        if (source.charAt(i) == target.charAt(j)) {
            return editorString(source, target, i - 1, j - 1);
        }

        //替换
        int t = 1 + editorString(source, target, i - 1, j - 1);
        //插入
        t = Math.min(1 + editorString(source, target, i, j - 1), t);
        //删除
        t = Math.min(1 + editorString(source, target, i - 1, j), t);
        return t;
    }


    @Test
    public void testEditorString() {
        String s1 = "horse";
        String s2 = "ros";
        System.out.println(editorString(s2, s1, s2.length() - 1, s1.length() - 1));

    }


    private int editorStringDp(String source, String target) {

        int[][] dp = new int[source.length() + 1][target.length() + 1];

        for (int i = 1; i <= source.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 1; j < target.length() + 1; j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= source.length(); i++) {
            for (int j = 1; j <= target.length(); j++) {
                if (source.charAt(i - 1) == target.charAt(j - 1))
                    dp[i][j] = dp[i - 1][j - 1];
                else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + 1, dp[i - 1][j] + 1);
                    dp[i][j] = Math.min(dp[i][j] , dp[i][j - 1] + 1);

                }
            }
        }
        return dp[source.length()][target.length()];
    }

    @Test
    public void testEditorStringDp() {
        String s1 = "horse";
        String s2 = "ros";
        System.out.println(editorStringDp(s2, s1));

    }

    @Test
    public void testEditorStringWithLog() {
        String s1 = "horse";
        String s2 = "ros";
        List<String> list = new ArrayList<String>();
        System.out.println(editorStringWithLog(s2, s1, s2.length() - 1, s1.length() - 1, list));
        list.forEach(System.out::println);
    }


    private int editorStringWithLog(String source, String target, int i, int j, List<String> list) {
        if (i < 0) return j + 1;
        if (j < 0) return i + 1;

        if (source.charAt(i) == target.charAt(j)) {
            return editorStringWithLog(source, target, i - 1, j - 1, list);
        }

        int[] flag = new int[]{999, 999, 999};
        List<String> l1 = new ArrayList<>();
        List<String> l2 = new ArrayList<>();
        List<String> l3 = new ArrayList<>();

        //替换
        flag[0] = 1 + editorStringWithLog(source, target, i - 1, j - 1, l1);
        //插入
        flag[1] = 1 + editorStringWithLog(source, target, i, j - 1, l2);
        //删除
        flag[2] = 1 + editorStringWithLog(source, target, i - 1, j, l3);

        int min = 999;
        int f = 0;
        for (int k = 0; k < 3; k++) {
            if (min > flag[k]) {
                f = k;
                min = flag[k];
            }
        }

        switch (f) {
            case 0:
                list.add(String.format("%s在%s上将%s替换成%s", source, i, source.charAt(i), target.charAt(j)));
                list.addAll(l1);
                break;
            case 1:
                list.add(String.format("%s在%s的%s后插入%s", source, i, source.charAt(i), target.charAt(j)));
                list.addAll(l2);
                break;
            case 2:
                list.add(String.format("%s在%s上删除%s", source, i, source.charAt(i)));
                list.addAll(l3);
        }
        return min;
    }
}
