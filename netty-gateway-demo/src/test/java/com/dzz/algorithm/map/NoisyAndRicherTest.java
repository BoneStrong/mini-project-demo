package com.dzz.algorithm.map;

import org.junit.Test;

import java.util.*;

public class NoisyAndRicherTest {
    /*
     *
     * 有一组 n 个人作为实验对象，从 0 到 n - 1 编号，其中每个人都有不同数目的钱，以及不同程度的安静值（quietness）。
     * 为了方便起见，我们将编号为x的人简称为 "personx"。
     *
     * 给你一个数组 richer ，其中 richer[i] = [ai, bi] 表示 personai比 personbi更有钱。
     * 另给你一个整数数组 quiet ，其中quiet[i] 是 person i 的安静值。
     * richer 中所给出的数据 逻辑自恰（也就是说，在 person x 比 person y 更有钱的同时，不会出现 person y 比 person x 更有钱的情况 ）。
     *
     * 现在，返回一个整数数组 answer 作为答案，其中answer[x] = y的前提是，
     * 在所有拥有的钱肯定不少于personx的人中，persony是最安静的人（也就是安静值quiet[y]最小的人）。
     *
     *
     *
     * 示例 1：
     *
     * 输入：richer = [[1,0],[2,1],[3,1],[3,7],[4,3],[5,3],[6,3]], quiet = [3,2,5,4,6,1,7,0]
     * 输出：[5,5,2,5,4,5,6,7]
     * 解释：
     * answer[0] = 5，
     * person 5 比 person 3 有更多的钱，person 3 比 person 1 有更多的钱，person 1 比 person 0 有更多的钱。
     * 唯一较为安静（有较低的安静值 quiet[x]）的人是 person 7，
     * 但是目前还不清楚他是否比 person 0 更有钱。
     * answer[7] = 7，
     * 在所有拥有的钱肯定不少于 person 7 的人中（这可能包括 person 3，4，5，6 以及 7），
     * 最安静（有较低安静值 quiet[x]）的人是 person 7。
     * 其他的答案也可以用类似的推理来解释。
     * 示例 2：
     *
     * 输入：richer = [], quiet = [0]
     * 输出：[0]
     *
     *
     * */
    @Test
    public void test() {

        System.out.println(Arrays.toString(loudAndRich(
                new int[][]{{1, 0}, {2, 1}, {3, 1}, {3, 7}, {4, 3}, {5, 3}, {6, 3}}
                , new int[]{3, 2, 5, 4, 6, 1, 7, 0})));
    }


    private int[] noisyAndRicher(int[][] arr, int[] quietness) {
        return null;
    }

    public int[] loudAndRich(int[][] richer, int[] quiet) {
        // 拓扑排序：取入度为0的先入队，减少它下游节点的入度，如果为0了也入队，直到队列中没有元素为止

        int n = quiet.length;

        // 先整理入度表和邻接表
        int[] inDegree = new int[n];

        List<Integer>[] g = new List[n];
        for (int i = 0; i < n; i++) {
            g[i] = new ArrayList<>();
        }

        for (int[] r : richer) {
            inDegree[r[1]]++;
            g[r[0]].add(r[1]);
        }

        // 初始化ans各位为自己
        // 题目说的是：在所有拥有的钱肯定不少于 person x 的人中，person y 是最安静的人
        // 注意这里的不少于，说明可以是自己
        int[] ans = new int[n];
        for (int i = 0; i < n; i++) {
            ans[i] = i;
        }

        // 拓扑排序开始
        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.offer(i);
            }
        }

        while (!queue.isEmpty()) {
            int p = queue.poll();
            // q是p的下游，也就是p比q有钱
            for (int q : g[p]) {
                // 如果p的安静值比q小，更新p的安静值对应的那个人
                // 注意这里p的安静值，并不是原始的quiet数组中的quiet[p]
                // 而是已经更新后的安静值，所以，应该取quiet[ans[p]]
                // 这里没有改变原来数组的内容，而是通过ans[p]间接引用的，细细品一下
                // 想像一下上图中的3的安静值被更新成了5的1
                if (quiet[ans[p]] < quiet[ans[q]]) {
                    ans[q] = ans[p];
                }

                if (--inDegree[q] == 0) {
                    queue.offer(q);
                }
            }
        }

        return ans;
    }
}
