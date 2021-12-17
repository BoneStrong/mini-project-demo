package com.dzz.algorithm.backtrack;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FullArrangementTest {

    /*
     * 全排列1
     * 给定一个没有重复数字的数组，返回全排列
     *
     * 示例：
     * 输入：[1,2,3]
     * 输出: [123,132,213,231,312,321]
     *
     *
     * 解析：
     * 回溯法：每次选择一个数，然后从剩下的里面选
     *
     * 容器法回溯，空间复杂度太高，采用交换法比较优雅
     * */

    public void fullArrangement1(List<Integer> list, List<Integer> target, int len) {
        if (target.size() == len) {
            System.out.println(target);
            return;
        }
        int size = list.size();
        for (int i = 0; i < size; i++) {
            List<Integer> temp = new ArrayList(list);
            List<Integer> tt = new ArrayList(target);
            target.add(list.get(i));
            list.remove(i);
            fullArrangement1(new ArrayList<>(list), target, len);
            list = temp;
            target = tt;

        }
    }

    @Test
    public void test1_1() {
        fullArrangement1(new ArrayList<>(Arrays.asList(1, 2, 3)), new ArrayList<Integer>(0), 3);
        System.out.println("==================");
        fullArrangement1(new ArrayList<>(Arrays.asList(1, 2, 3, 4)), new ArrayList<Integer>(0), 4);
    }

    /*
     * 全排列1
     * 交换的方式才是最优雅的
     * 每次确认一个位置的数字，之后只递归后面的数字
     * */
    private void upset(int[] nums, int begin) {
        if (begin == nums.length) {
            System.out.println(Arrays.toString(nums));
        }
        for (int i = begin; i < nums.length; i++) {
            swap(nums, i, begin);
            upset(nums, begin + 1);
            swap(nums, i, begin);
        }

    }

    private void swap(int[] nums, int i, int begin) {
        int temp = nums[i];
        nums[i] = nums[begin];
        nums[begin] = temp;
    }

    @Test
    public void test1_2() {
        upset(new int[]{1, 2, 3}, 0);
    }


    /*
     *
     * 全排列2
     * 给定一个可包含重复数字的序列 nums ，按任意顺序 返回所有不重复的全排列
     *
     * 示例 1：
     * 输入：nums = [1,1,2]
     * 输出：
     * [[1,1,2],[1,2,1],[2,1,1]]
     *
     * */

    public void test2_1() {

    }
}
