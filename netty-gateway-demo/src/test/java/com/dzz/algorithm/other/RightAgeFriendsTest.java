package com.dzz.algorithm.other;

import org.junit.Test;

public class RightAgeFriendsTest {

    /*
     * 人们会互相发送好友请求，现在给定一个包含有他们年龄的数组，ages[i]表示第i个人的年龄。
     *
     * 当满足以下条件时，A 不能给 B（A、B不为同一人）发送好友请求：
     *
     * age[B] <= 0.5 * age[A] + 7
     * age[B] > age[A]
     * age[B] > 100 && age[A] < 100
     * 否则，A 可以给 B 发送好友请求。
     *
     * 注意如果 A 向 B 发出了请求，不等于 B 也一定会向 A 发出请求。而且，人们不会给自己发送好友请求。
     *
     * 求总共会发出多少份好友请求?
     *
     * 示例 1:
     *
     * 输入: [16,16]
     * 输出: 2
     * 解释: 二人可以互发好友申请。
     * 1
     * 2
     * 3
     * 示例 2:
     *
     * 输入: [16,17,18]
     * 输出: 2
     * 解释: 好友请求可产生于 17 -> 16, 18 -> 17.
     * 1
     * 2
     * 3
     * 示例 3:
     *
     * 输入: [20,30,100,110,120]
     * 输出: 3
     * 解释: 好友请求可产生于 110 -> 100, 120 -> 110, 120 -> 100.
     * 1
     * 2
     * 3
     * 说明:
     *
     * 1 <= ages.length <= 20000.
     * 1 <= ages[i] <= 120.
     *
     * */
    @Test
    public void test(){

    }

    /*
    *
    * 思路：
    * 根据题意可以知道
    * age[A]> age[B] > 0.5 * age[A] + 7
    *
    * 对于A年龄的人来说，只要知道 （0.5 * age[A] + 7，age[A]）范围内的人数，就能知道他会发送多少条信息
    *
    * 所以可以按年龄排序，记录每个年龄的人数，利用前缀和，可以O（1）的复杂度求出年龄范围的人数
    * */
    public void rightAge(){

    }
}
