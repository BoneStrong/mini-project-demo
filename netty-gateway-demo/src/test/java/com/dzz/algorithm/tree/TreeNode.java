package com.dzz.algorithm.tree;

public class TreeNode {

    private TreeNode left;

    private TreeNode right;

    private int val;

    public TreeNode getLeft() {
        return left;
    }

    public TreeNode setLeft(TreeNode left) {
        this.left = left;
        return this;
    }

    public TreeNode getRight() {
        return right;
    }

    public TreeNode setRight(TreeNode right) {
        this.right = right;
        return this;
    }

    public int getVal() {
        return val;
    }

    public TreeNode setVal(int val) {
        this.val = val;
        return this;
    }
}
