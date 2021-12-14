package com.dzz.algorithm.tree;

import org.junit.Test;

public class SameWayTest {

    class ThreeTuple {
        Integer val; //最大边数的值
        int left;//左边的边数
        int right;//右边的边数

        public ThreeTuple(Integer val, int left, int right) {
            this.val = val;
            this.left = left;
            this.right = right;
        }

        public Integer getVal() {
            return val;
        }

        public ThreeTuple setVal(Integer val) {
            this.val = val;
            return this;
        }

        public int getLeft() {
            return left;
        }

        public ThreeTuple setLeft(int left) {
            this.left = left;
            return this;
        }

        public int getRight() {
            return right;
        }

        public ThreeTuple setRight(int right) {
            this.right = right;
            return this;
        }
    }

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
    private ThreeTuple sameWay(TreeNode t) {
        if (t == null) return null;

        ThreeTuple leftT = sameWay(t.getLeft());
        ThreeTuple rightT = sameWay(t.getRight());

        if (leftT == null && rightT == null) return new ThreeTuple(t.getVal(), 0, 0);

        if (leftT == null)
            leftT = new ThreeTuple(null, 0, 0);
        if (rightT == null)
            rightT = new ThreeTuple(null, 0, 0);

        //左右值和父节点值一样
        if (t.getLeft() != null && t.getRight() != null
                && (t.getLeft().getVal() == t.getRight().getVal())
                && leftT.getVal() == t.getVal()
        ) {
            //左右边值也是一样

        }


        if (t.getLeft() != null) {
            if (t.getLeft().getVal() == leftT.val && leftT.val == t.getVal()) {

            }
        }

        return null;
    }

    @Test
    private void test() {

    }
}
