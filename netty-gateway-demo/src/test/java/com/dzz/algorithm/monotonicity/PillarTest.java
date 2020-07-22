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
     * ʹ�õ���ջ,����ջ�������ǿ����ҵ���ǰԪ�صĺ����һ�����ڻ���С������Ԫ�أ�
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
     * ����Ӧ���õ�������ջ���ҵ������һ������С��Ԫ��
     * */
    @Test
    public void test() {
        int[] zuZhi = {6, 4, 5, 2, 4, 3, 9};
        Stack<Integer> stack = new Stack<>();
        //��������ջ�±�
        int[] right = new int[zuZhi.length];
        int[] left = new int[zuZhi.length];
        for (int i = 0; i < zuZhi.length; i++) {
            while (!stack.isEmpty() && zuZhi[i] < zuZhi[stack.peek()]) {
                right[stack.pop()] = i;
            }
            stack.push(i);
            right[i] = -1;//-1��ʾ��û��������taС����
        }

        stack = new Stack<>();
        for (int i = zuZhi.length - 1; i >= 0; i--) {
            while (!stack.isEmpty() && zuZhi[i] < zuZhi[stack.peek()]) {
                left[stack.pop()] = i;
            }
            stack.push(i);
            left[i] = -1;//-1��ʾ��û����������С����
        }
        System.out.println(Arrays.toString(left));
        System.out.println(Arrays.toString(right));

        //�������ұ߽� ��������С
        int max = 0;
        for (int i = 0; i < zuZhi.length; i++) {
            int l = left[i] == -1 ? 0 : left[i] + 1;
            int r = right[i] == -1 ? zuZhi.length : right[i];
            max = Math.max(max, (r - l) * zuZhi[i]);
        }
        System.out.println(max);
    }

    /*
     * �Ż�˼·
     * ���Ǽ�����������ʱ���õ�����������ջ��
     * �ֱ������ĳһ���߶����������ܹ����쵽����Զ���룬��ʵ�Ⲣû�б�Ҫ��
     * ��Ϊ������һ��ջҲ����ͬʱ��������ߵı߽硣
     * �ٸ����ӣ�[1, 3, 6, 7]����ǰԪ����5��������Ҫ��6��7��ջ��5��ջ��
     * ����֪����5����߽���3������ϸ��һ�룬����7��˵������֪�����������ұ߽硣7����߽���6���ұ߽���5��
     * Ҳ����˵����ջ����Ԫ�ض��ԣ�������߽���stack[top-1]���ұ߽��ǵ�ǰ��λ��i�������i - stack[top-1] - 1��
     * */
    @Test
    public void test2() {
        int[] zuZhi = {6, 4, 5, 2, 4, 3, 9};
        Stack<Integer> stack = new Stack<>();
        //��������ջ�±�
        int[] right = new int[zuZhi.length];
        int[] left = new int[zuZhi.length];
        for (int i = 0; i < zuZhi.length; i++) {
            while (!stack.isEmpty() && zuZhi[i] < zuZhi[stack.peek()]) {
                right[stack.pop()] = i;
            }
            if (stack.isEmpty()) {
                left[i] = -1;
            } else {
                left[i] = stack.peek();
            }

            stack.push(i);
            right[i] = -1;//-1��ʾ��û��������taС����

        }


        //�������ұ߽� ��������С
        int max = 0;
        for (int i = 0; i < zuZhi.length; i++) {
            int l = left[i] == -1 ? 0 : left[i] + 1;
            int r = right[i] == -1 ? zuZhi.length : right[i];
            max = Math.max(max, (r - l) * zuZhi[i]);
        }
        System.out.println(max);
    }

    /*
     * ����
     * �����1��������
     *
     * ��ÿһ�е�������ֱ��ͼ��������1��Ϊֱ��ͼ�����Ӹ߶�
     * */
    @Test
    public void testMax() {
        int[][] arr = {
                {1, 0, 1, 0, 0},
                {1, 0, 1, 1, 1},
                {1, 1, 1, 1, 1},
                {1, 0, 0, 1, 0}
        };

        int n = arr.length;
        int m = arr[0].length;
        int[][] height = new int[n][m];
        int max = 0;
        Stack<Integer> stack = new Stack<>();

        for (int i = 0; i < n; i++) {
            //ÿһ�е�ֱ��ͼ
            for (int j = 0; j < m; j++) {
                //Ԥ����
                if (i == 0) {
                    height[i][j] = arr[i][j];
                } else {
                    height[i][j] = arr[i][j] == 0 ? 0 : height[i - 1][j] + 1;
                }
            }

            int[] right = new int[m];
            int[] left = new int[m];
            //ÿһ�е�������
            for (int j = 0; j < m; j++) {
                while (!stack.isEmpty() && height[i][j] < height[i][stack.peek()]) {
                    right[stack.pop()] = j;
                }
                if (stack.isEmpty()) {
                    left[j] = -1;
                } else {
                    left[j] = stack.peek();
                }
                stack.push(j);
                right[j] = -1;
            }

            for (int j = 0; j < m; j++) {
                int l = left[j] == -1 ? 0 : left[j] + 1;
                int r = right[j] == -1 ? m : right[j];
                max = Math.max(max, (r - l) * height[i][j]);
            }
        }

        System.out.println(max);

    }
}
