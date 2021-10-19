package com.dzz.algorithm.string;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class StringTest {

    @Test
    public void bigNumStringToLong() {
        //参考jdk,按字符一个个去判断
        Character aa = '1';
        int digit = Character.digit(aa, 10);
        System.out.println(digit);
        System.out.println(Long.parseLong("1000"));
    }

    /*
     *
     * 代码思路也简单，就是把两个数的每一位都取出来相乘，num1的第i位和num2的第j位相乘，
     * 放入一个数组，放入的位置为i + j，先不考虑进位问题，所有数字处理完毕之后，再考虑进位问题。
     *
     *        String s1 = "12484845665488468464";
        String s2 = "84364351456456";
     *
     * */


    @Test
    public void testBigStringNumMultiplication() {
        System.out.printf("%s : %s", bigStringNumMultiplication("123", "4567"), 123 * 4567);
    }


    public String bigStringNumMultiplication(String s1, String s2) {
        String rs = "0";
        StringBuilder sb = new StringBuilder();
        for (int i = s2.length() - 1; i >= 0; i--) {
            rs = addStringNum(rs, mulNunChar(s1, s2.charAt(i)) + sb.toString());
            sb.append("0");
        }
        return rs;
    }

    @Test
    public void testMulNunChar() {
        System.out.println(mulNunChar("123456", '6'));
        System.out.println(123456 * 6);
        System.out.println(mulNunChar("78943496496498", '7'));
        System.out.println(78943496496498L * 7);

        System.out.println(mulNunChar("123", '7'));
    }


    private String mulNunChar(String s1, Character c1) {
        String rs = "0";
        StringBuilder suf = new StringBuilder();
        for (int i = s1.length() - 1; i >= 0; i--) {
            String temp = Character.digit(s1.charAt(i), 10) * Character.digit(c1, 10) + suf.toString();
            rs = addStringNum(temp, rs);
            suf.append("0");
        }
        return rs;
    }

    @Test
    public void testAddStringNum() {
        System.out.println(addStringNum("123", "456"));
        System.out.println(addStringNum("123443262346", "456"));
    }


    private String addStringNum(String s1, String s2) {
        List<String> list = new ArrayList<>();
        int add = 0;
        for (int i = s1.length() - 1, j = s2.length() - 1, pos = Math.max(i, j) + 1; pos > 0; pos--, i--, j--) {
            int t1 = i < 0 ? 0 : Character.digit(s1.charAt(i), 10);
            int t2 = j < 0 ? 0 : Character.digit(s2.charAt(j), 10);
            int t = t1 + t2 + add;
            add = t > 9 ? 1 : 0;
            list.add(t % 10 + "");
        }
        boolean flag = true;
        StringBuilder sb = new StringBuilder();
        for (int k = list.size() - 1; k >= 0; k--) {
            if (flag) {
                if (list.get(k).equals("0")) continue;
                flag = false;
            }
            sb.append(list.get(k));
        }
        return sb.toString();
    }



}
