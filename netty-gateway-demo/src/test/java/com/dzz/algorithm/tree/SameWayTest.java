package com.dzz.algorithm.tree;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class SameWayTest {

    /*
     * 给定一个二叉树，找到最长的路径，这个路径中的每个节点具有相同值。
     * 这条路径可以经过也可以不经过根节点。
     * 注意：两个节点之间的路径长度由它们之间的边数表示。
     *
     * 示例1：
     * -     5
     * -   /  |
     * -  3   5
     * - /|   |
     * -1 2   5
     * 输出：2  5-5-5
     *
     * 示例1：
     * -     5
     * -   /  |
     * -  4   3
     * - /|   |
     * -4 4   5
     * 输出：2  4-4-4
     *
     *
     * 解析：
     * 中序遍历，获取子节点的边和最大边数，假设子节点和父节点的值一致
     *
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

        sameValueWay(treeNode, v);
        System.out.println(v.get());


        v.set(0);
        TreeNode treeNode2 = new TreeNode().setVal(5)
                .setLeft(new TreeNode().setVal(4)
                        .setLeft(new TreeNode().setVal(4))
                        .setRight(new TreeNode().setVal(4)))
                .setRight(new TreeNode().setVal(3)
                        .setRight(new TreeNode().setVal(5)));

        sameValueWay(treeNode2, v);
        System.out.println(v.get());


    }


    /*
     * 如果子节点和父节点的值一样，那么父节点的同值路径=子节点的同源路径+1
     * 否则，父节点的同源路径=0；
     * 使用一个全局变量保留树中出现的最大路径值，这样遍历一次即可求出最大同源路径值
     * */
    public int sameValueWay(TreeNode treeNode, AtomicInteger v) {
        if (treeNode == null) return 0;
        int left = 0;
        int right = 0;
        if (treeNode.getLeft() != null) {
            int leftValue = sameValueWay(treeNode.getLeft(), v);
            if (treeNode.getVal() == treeNode.getLeft().getVal())
                left = 1 + leftValue;
        }
        if (treeNode.getRight() != null) {
            int rightValue = sameValueWay(treeNode.getRight(), v);
            if (treeNode.getVal() == treeNode.getRight().getVal())
                right = 1 + rightValue;
        }
        //如果左右节点值相等，那么left,right必然！=0；如果其中左右不相当，left和right必然有一个=0；
        //那么当前节点的最大同源路径就是left+right
        v.set(Math.max(v.get(), left + right));
        return Math.max(left, right);
    }
}
