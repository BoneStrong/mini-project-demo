package com.dzz.algorithm.monotonicity;

import org.junit.Test;

import java.util.Arrays;
import java.util.Stack;

/**
 * @author zoufeng
 * @date 2020-7-21
 */
public class PillarTest {

    /*
     * ��һ�����ӣ��߶�Ϊ[6, 4, 5, 2, 4, 3, 9]
     * �������ܹ��ɵ�������
     *
     * ˼·��
     * ����ջ,����ջ�������ǿ����ҵ���ǰԪ�ص��¸���������Ԫ�أ�
     * �ڵ�ǰԪ��������ʼ���ܵõ��Ե�ǰԪ�ظߵľ���
     *
     * ����������Ҫ֪����ߺ��ұߵı߽�
     *
     * ������������һ���򵥵ĵ���ջ
     * ���ӣ�[6, 4, 5, 2, 4, 3, 9]����¼���������飺[0,0,0,0,0,0,0]
     * ����һ�������ݼ��ĵ���ջ,�����Ԫ�������ջ��С����ջ����ջ������ȡ��ջ����Ԫ�أ�ֱ��ջ��Ԫ�رȷ����Ԫ�ش�
     * ��ÿ��ջ��Ԫ��ȡ��ʱ����ʾ�����˱������Ԫ�ؼ����ջ�� ջ�����Ԫ�������ӵ�����
     * �����һ��Ԫ��ʱ��ջΪ�գ�����ջ��
     *
     *
     * */
    @Test
    public void test() {
        int[] zuZhi = {6, 4, 5, 2, 4, 3, 9};
        Stack<Integer> stack = new Stack<>();
        //�����ݼ�ջ�±�
        int[] arr = new int[zuZhi.length];
        for (int i = 0; i < zuZhi.length; i++) {
            while (!stack.isEmpty() && zuZhi[i] > zuZhi[stack.peek()]) {
                arr[stack.pop()] = i;
            }
            stack.push(i);
            arr[i] = -1;//-1��ʾ��û���������������
        }
        System.out.println(Arrays.toString(arr));
    }
}
