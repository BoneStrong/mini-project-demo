package com.dzz.algorithm.greedy;

import org.junit.Test;

import java.util.*;

public class Course {

    /*
     * 课程表三
     *  这里有 n 门不同的在线课程，按从 1 到 n编号。
     * 给你一个数组 courses ，其中 courses[i] = [durationi, lastDayi]
     * 表示第 i 门课将会 持续 上 durationi 天课，并且必须在不晚于 lastDayi 的时候完成。
     *
     * 你的学期从第 1 天开始。且不能同时修读两门及两门以上的课程。
     *
     * 返回你最多可以修读的课程数目。
     *
     * 示例：
     * 示例 1：
     *
     * 输入：courses = [[100, 200], [200, 1300], [1000, 1250], [2000, 3200]]
     * 输出：3
     * 解释：
     * 这里一共有 4 门课程，但是你最多可以修 3 门：
     * 首先，修第 1 门课，耗费 100 天，在第 100 天完成，在第 101 天开始下门课。
     * 第二，修第 3 门课，耗费 1000 天，在第 1100 天完成，在第 1101 天开始下门课程。
     * 第三，修第 2 门课，耗时 200 天，在第 1300 天完成。
     * 第 4 门课现在不能修，因为将会在第 3300 天完成它，这已经超出了关闭日期。
     * 示例 2：
     *
     * 输入：courses = [[1,2]]
     * 输出：1
     * 示例 3：
     *
     * 输入：courses = [[3,2],[4,3]]
     * 输出：0
     *
     *
     * */
    @Test
    public void test() {
        int[][] arr = {{100, 200}, {200, 1300}, {1000, 1250}, {2000, 3200}};
//        System.out.println(course3(arr));
        System.out.println(scheduleCourse(arr));
    }

    public class Pair {
        int k;
        int v;

        public Pair(int k, int v) {
            this.k = k;
            this.v = v;
        }

        public int getK() {
            return k;
        }

        public Pair setK(int k) {
            this.k = k;
            return this;
        }

        public int getV() {
            return v;
        }

        public Pair setV(int v) {
            this.v = v;
            return this;
        }
    }

    /*
     * 解析：
     * 首先可以排除一部分不合法的课程，比如持续时间大于结束时间的课程
     *
     * 这个题目可以理解成一条绳子上找不相交的线段，或者是一个时间轴上不相交的区间
     * 那么可以先按结束时间排序，在时间轴上设置好结束节点
     * 比如示例1的 【1，200，1250，1300，3200】
     * 那么我们在3200这个节点可以选的最大课程数是dp[3200]=dp[1300]+ (1300+2000)>3200?0:1
     *
     * 总结：
     * 其实这个用的是贪心策略，按结束时间排序选课后，
     * 如果新的一门课无法选择，可以将前面可选课程的的结束时间，作为他的结束时间
     * ，保证后面尽量可以选择更多的课程。
     *
     * 本题下面题解的while循环是为了找到最小的结束时间，这里可以用优先队列来替代
     *
     * */
    private int course3(int[][] arr) {
        if (arr[0].length != 2) return 0;
        List<Pair> list = new ArrayList<Pair>();
        for (int[] ints : arr) {
            if (ints[0] < ints[1]) {
                list.add(new Pair(ints[0], ints[1]));
            }
        }
        if (list.isEmpty()) return 0;
        list.sort(Comparator.comparingInt(o -> o.v));
        int[] dp = new int[list.size() + 1];
        int[] dpV = new int[list.size() + 1];//存结束点坐标

        for (int i = 1; i < list.size() + 1; i++) {
            int j = i - 1;
            int temp = dp[j];
            while (j >= 0) {
                if ((dpV[j] + list.get(i - 1).k) > list.get(i - 1).v) {//选不了这门课
                    if (dp[j] >= temp) {
                        temp = dp[j];
                        dpV[i] = dpV[j];
                    }
                } else {//可以选
                    if (dp[j] + 1 >= temp) {
                        temp = dp[j] + 1;
                        dpV[i] = dpV[j] + list.get(i - 1).k;//选课的结束时间
                    }
                }
                j--;
            }
            dp[i] = temp;
        }

        return dp[dp.length - 1];
    }

    /*
     * 大根堆
     *
     * courses = [[100, 200], [1000, 1250], [200, 1300], [2000, 3200]]
     *
     * 时间复杂度：O(nlogn)
     * 空间复杂度：O(n)
     * */
    public int scheduleCourse(int[][] courses) {
        Arrays.sort(courses, Comparator.comparingInt(a -> a[1]));//结束时间排序
        PriorityQueue<Integer> q = new PriorityQueue<>((a, b) -> b - a);
        int sum = 0;//起始时间
        for (int[] c : courses) {
            int d = c[0], e = c[1];//持续时间d,结束时间e
            sum += d;
            q.add(d);
            if (sum > e) sum -= q.poll(); //学习总时长剔除最长的课程
            //因为课程的贡献度都是1，如果当前学的课程不符合话，
            // 可以去掉现在和之前学习时间最长的课程，以便腾出时间后面去学习更多的课程
        }
        return q.size();
    }


}
