package com.dzz.algorithm.monotonicity;

import org.junit.Test;

import java.util.*;

/**
 * @author zoufeng
 * @date 2020-7-24
 */
public class QueueTest {

    /*
     * 题目
     * 数组中右边第一个比他大元素（时间复杂度O（N））
     *
     * 本质就是写个单调栈.
     *
     * 思路：
     * 首先准备一个空栈，放入第一个元素作为栈顶
     * 后面的元素放入时比较栈顶
     * 如果比栈顶元素小，则直接入栈，变成栈顶元素
     * 如果放入的数字比栈顶大，则移出栈顶的元素，直到栈顶的元素比如新加的元素大或者栈顶为空，说明新加的元素是第一个比栈顶大的数
     *
     * */
    @Test
    public void test() {
        int[] arr = {5, 1, 4, 8, 3, 6, 2, 0};
        int[] result = new int[arr.length];//[8, 4, 8, -1, 6, -1, -1, -1]
        Arrays.fill(result, -1);

        Stack<Integer> stack = new Stack<>();
        for (int i = 0; i < arr.length; i++) {
            //栈顶元素小于新加元素，说明找到了比栈顶元素大的值
            while (!stack.isEmpty() && arr[stack.peek()] < arr[i]) {
                result[stack.pop()] = arr[i];
            }
            stack.push(i);
        }
        System.out.println(Arrays.toString(result));
    }


    /*
     * 进阶
     * LeetCode 315. 计算右侧小于当前元素的个数
     *
     * 统计求最值看样子不太适合单调栈
     *
     * 这道题可以使用二叉搜索树
     * 二叉搜索树
     * 使用二叉搜索树也可以完成插入并统计的功能，从右往左构建二叉树。
     *
     * 递归实现：
     * 当走到右节点时，
     * （1）统计根节点和左节点的个数，
     * （2）继续插入并统计右边是否还有较小值；
     * 当走到左节点或者根节点时，
     * （1）计数器加一，
     * （2）继续插入并统计左边是否还有较小值。
     *
     * 遍历一遍就可以完成搜索，时间复杂度O(nlogn)。
     *
     *
     * */
    @Test
    public void countRightMin() {
        final int[] arr = {5, 1, 4, 8, 3, 6, 2, 0};
        List<List<Integer>> lRef = new ArrayList<>(arr.length);//存储比他大的引用
        for (int i = 0; i < arr.length; i++) {
            lRef.add(new ArrayList<>());
        }

        int[] count = new int[arr.length];
        ArrayDeque<Integer> deque = new ArrayDeque<>();

        for (int i = 0; i < arr.length; i++) {
            while (!deque.isEmpty() && arr[deque.peek()] > arr[i]) {
                //新加元素小，那么出栈的肯定比他大
                Integer j = deque.pop();
                lRef.get(i).add(j);
                count[j]++;

            }
            if (deque.isEmpty()) {
                deque.push(i);
            } else {
                deque.addLast(i);
                while (i != deque.peek()) {
                    //新加的数是否比栈顶的引用数要小，如果小，需要更新引用
                    Integer pop = deque.pop();
                    List<Integer> list = lRef.get(pop);
                    for (Integer j : list) {
                        if (arr[i] < arr[j]) {
                            count[j]++;
                            lRef.get(i).add(j);
                        }
                    }
                    deque.addLast(pop);
                }

            }

        }
        System.out.println(Arrays.toString(count));
    }

}
