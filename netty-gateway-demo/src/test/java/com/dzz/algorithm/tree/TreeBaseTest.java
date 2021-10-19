package com.dzz.algorithm.tree;

import org.junit.Before;
import org.junit.Test;

public class TreeBaseTest {


    /*
    * （1）先序遍历：先访问根节点，再访问左子树，最后访问右子树。

          (2)  后序遍历：先左子树，再右子树，最后根节点。

        （3）中序遍历：先左子树，再根节点，最后右子树。

        （4）层序遍历：每一层从左到右访问每一个节点。
        *
        *这个遍历不要太简单！
    * */
    private TreeNode root;

    @Before
    public void init() {
        root = new TreeNode().setVal(30)
                .setLeft(new TreeNode().setVal(10)
                        .setRight(new TreeNode().setVal(20)))
                .setRight(new TreeNode().setVal(60)
                        .setLeft(new TreeNode().setVal(40)
                                .setRight(new TreeNode().setVal(50)))
                        .setRight(new TreeNode().setVal(70)
                                .setRight(new TreeNode().setVal(80))));

    }

    @Test
    public void test() {
        pre(root);
        System.out.println("-----------");
        mid(root);
        System.out.println("-----------");
        after(root);
    }

    private void pre(TreeNode root) {
        if (root == null) return;
        TreeNode left = root.getLeft();
        TreeNode right = root.getRight();
        System.out.println(root.getVal());
        pre(left);
        pre(right);
    }

    private void mid(TreeNode root) {
        if (root == null) return;
        TreeNode left = root.getLeft();
        TreeNode right = root.getRight();
        mid(left);
        System.out.println(root.getVal());
        mid(right);
    }

    private void after(TreeNode root) {
        if (root == null) return;
        TreeNode left = root.getLeft();
        TreeNode right = root.getRight();
        after(left);
        after(right);
        System.out.println(root.getVal());
    }
}
