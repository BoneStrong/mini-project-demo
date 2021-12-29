package com.dzz.algorithm.linklist;

import org.junit.Before;
import org.junit.Test;

/**
 * @author zoufeng
 * @date 2020-1-14
 */
public class ReverseLinkList {

    private ListNode head;

    @Before
    public void init() {
        head = new ListNode(1, new ListNode(2, new ListNode(3, new ListNode(4, new ListNode(5, null)))));
    }

    @Test
    public void simple1() {
//        ListNode listNode = reverse1(head);
        ListNode listNode = reverse(head);
        do {
            System.out.println(listNode.getValue());
            listNode = listNode.getNext();
        } while (listNode != null);
    }

    /*
     *  pre   cur
     *   |     |
     * head--node--node--tail
     *
     * 双指针
     * */
    public ListNode reverse(ListNode head) {
        ListNode pre = head;
        ListNode now = head.getNext();
        pre.setNext(null);
        while (now != null) {
            ListNode next = now.getNext();
            now.setNext(pre);
            if (next == null) {
                break;
            }
            pre = now;
            now = next;
        }
        return now;
    }

    /*
     *
     * 反转从位置 m 到 n 的链表。请使用一趟扫描完成反转。
     *
     * 说明:
     * 1 ≤ m ≤ n ≤ 链表长度。
     *
     * 示例:
     * 输入：1-2-3-4-5-null ,m=2,n=4
     * 输出：1-4-3-2-5-null
     *
     * */
    public ListNode reverse2(ListNode head, int m, int n) {

        ListNode h = null;
        ListNode e = null;
        ListNode hpre = null;
        ListNode eAfter = null;
        ListNode pre = head;
        ListNode cur = head.getNext();
        int count = 2;
        while (cur != null && cur.getNext() != null) {
            ListNode next = cur.getNext();

            if (count == m) {
                h = cur;
                hpre = pre;
                pre.setNext(null);
                cur.setNext(null);
            }
            if (count == n) {
                e = cur;
                eAfter = next;
            }

            if (m < count && count <= n) {
                cur.setNext(pre);
            }

            pre = cur;
            cur = next;
            count++;
        }
        hpre.setNext(e);
        h.setNext(eAfter);
        return head;
    }

    @Test
    public void testReverseLinkList2() {
        System.out.println(reverse2(head, 2, 4));
    }
}
