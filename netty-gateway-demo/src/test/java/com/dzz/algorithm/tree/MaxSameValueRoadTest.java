package com.dzz.algorithm.tree;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class MaxSameValueRoadTest {

    /*
     * 最大同值路径
     * 给定一个二叉树，找到最长的路径，这个路径中的每个节点具有相同值。 这条路径可以经过也可以不经过根节点。
     * 注意：两个节点之间的路径长度由它们之间的边数表示。
     *
     *
     * 【这里假设树的所有节点都是同一个值】
     * 示例1：
     * -     5
     * -   /  |
     * -  3   5
     * - /|   |
     * -1 2   5
     * 输出：4
     *
     * */
    @Test
    public void test() {
        AtomicInteger v = new AtomicInteger(0);
        TreeNode treeNode = new TreeNode().setVal(5)
                .setLeft(new TreeNode().setVal(3)
                        .setLeft(new TreeNode().setVal(1))
                        .setRight(new TreeNode().setVal(2)))
                .setRight(new TreeNode().setVal(5)
                        .setRight(new TreeNode().setVal(5)));

        treeNodeHigh(treeNode, v);
        System.out.println(v.get());
    }


    /*
     * 第一个思路是分别求左右子树的最大边数，相加就是最长的路径
     * 但是这个是有问题的，比如左子树经过父节点的最大边数可能没有不经过父节点的边数大。
     * 所以可以换个思路，从深度的角度求边。
     * 求左右子树的最大深度，找出相加最大的，即可求出最大的路径
     *
     * maxValue是全局变量引用
     *
     * */
    public int treeNodeHigh(TreeNode treeNode, AtomicInteger maxValue) {
        if (treeNode == null) return 0;
        int left = treeNode.getLeft() == null ? 0 : 1 + treeNodeHigh(treeNode.getLeft(), maxValue);
        int right = treeNode.getRight() == null ? 0 : 1 + treeNodeHigh(treeNode.getRight(), maxValue);
        if (left + right > maxValue.get()) {
            maxValue.set(left + right);
        }
        return Math.max(left, right);
    }
}
