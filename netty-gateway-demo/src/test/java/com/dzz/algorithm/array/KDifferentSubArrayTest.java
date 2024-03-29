package com.dzz.algorithm.array;

import org.junit.Test;

public class KDifferentSubArrayTest {

    /*
     *
     * 992. K 个不同整数的子数组

     * 给定一个正整数数组 A，如果 A 的某个子数组中不同整数的个数恰好为 K，则称 A 的这个[连续、不一定不同的子数组]为好子数组。
     *
     * （例如，[1,2,3,1,2] 中有 3 个不同的整数：1，2，以及 3。）
     * 返回 A 中好子数组的数目。
     *
     * 示例 1：
     *
     * 输入：A = [1,2,1,2,3], K = 2
     * 输出：7
     * 解释：恰好由 2 个不同整数组成的子数组：[1,2], [2,1], [1,2], [2,3], [1,2,1], [2,1,2], [1,2,1,2].
     * 示例 2：
     *
     * 输入：A = [1,2,1,3,4], K = 3
     * 输出：3
     * 解释：恰好由 3 个不同整数组成的子数组：[1,2,1,3], [2,1,3], [1,3,4].
     *
     * */
    @Test
    public void test() {
        System.out.println(window(new int[]{1, 1, 2, 1, 1, 2, 2, 3}, 2));
    }

    /*
     * 思路及算法：
     *
     * 对于任意一个右端点，可能存在一系列左端点与其对应，满足两端点所指区间对应的子数组内恰有 K 个不同整数。
     * 因此可能有 O(n^2) 个子数组满足条件。
     *
     * 分析这些左端点，我们可以证明：【对于任意一个右端点，能够与其对应的左端点们必然相邻】。
     *
     * 证明非常直观，假设区间 [L1,r] 和 [L2,r] 为满足条件的数组（不失一般性，设 [L1<L2] ）。
     * 现在我们设存在一个 [L] 满足 [L1<L<L2] ，那么区间 [L,R] 作为 [L1,R] 的子数组，其中的不同整数数量必然不超过 K。
     * 同理，区间 [L,r] 作为 [L2,r] 的父数组，其中的不同整数数量必然不少于 K。那么可知区间 [L,r] 中的不同整数数量即为 K。
     *
     * 这样我们就可以用一个区间 [L1,L2]来代表能够与右端点 r 对应的左端点们。
     *
     * 同时，我们可以发现：当右端点向右移动时，左端点区间也同样向右移动。因为当我们在原有区间的右侧添加元素时，
     * 区间中的不同整数数量只会不变或增加，因此我们需要在区间左侧删除一定元素，以保证区间内整数数量仍然为 K。
     *
     * 于是我们可以用滑动窗口解决本题，和普通的滑动窗口解法的不同之处在于，
     * 我们需要记录两个左指针 [L1] 与 [L2]来表示左端点区间 [L1,L2] 。
     * 第一个左指针表示极大的包含 K 个不同整数的区间的左端点，第二个左指针则表示极大的包含 K-1 个不同整数的区间的左端点
     *
     *
     * 采用滑动窗口，对于每个A[right]，考虑两种情况：
     *
     * 1.一种是 A[right] 是已经出现过的数字，那么只要以A[right-1]为结尾的“好子数组”都肯定满足“好子数组”的条件，
     * 这个时候只需要记录以A[right-1]为结尾的“好子数组”的数目，然后加上left向右收缩得到的增量；
     *
     * 2.一种是 A[right] 是新的数字，那么需要将left往右收缩，满足当前滑动窗口内不同数字为K，
     * 然后left再尽可能往右缩，得到的增量就是当前情况的“好子数组”个数。
     *
     * 让我们通过一个例子来形象化上面的思路，考虑数组：1 1 2 1 1 2 2 3
     * 我们维护一个pre值，初始化为1
     *
     * a[2]=2，left向右缩到位置1，pre=2，ans+=pre，此时满足题意的有 1 1 2、1 2
     *
     * a[3]=1，left向右缩到位置2，pre=3，ans+=pre，此时满足题意的有 1 1 2 1、1 2 1、2 1
     *
     * a[4]=1，left不用缩，pre=3，ans+=pre，此时满足题意的有 1 1 2 1 1、1 2 1 1、2 1 1
     *
     * a[5]=2，left向右缩到位置4，pre=5，ans+=pre，此时满足题意的有 1 1 2 1 1 2、1 2 1 1 2、2 1 1 2、1 1 2、1 2
     *
     * a[6]=2，left不用缩，pre=5，ans+=pre，此时满足题意的有 1 1 2 1 1 2 2、1 2 1 1 2 2、2 1 1 2 2、1 1 2 2、1 2 2
     *
     * a[7]=3，此时滑动窗口中不同数字大于K，先讲left缩到位置5满足要求，将pre置为1，然后再往右缩到位置6，pre=2，ans+=pre，此时满足题意得有 2 2 3、2 3
     *
     * 综上所述，当left不能缩的时候，就相当于是在之前统计的情况上后面加上当前数字；可以缩，就相当于多了left在向右缩的过程中出现的情况
     *
     * */
    public int window(int[] arr, int k) {
        int left1 = 0;//最大左边界
        int left2 = 1;//最小左边界,也就是缩进量
        int r = 0;//右边界
        int result = 0;//结果计数
        int[] num = new int[10];//假设只有0-9
        int count = 0;//用来不同数字的计数


        while (r < arr.length) {
            if (num[arr[r]] == 0) {
                count++;
            }
            num[arr[r]]++;
            if (count < k) {
                r++;
                continue;
            }

            if (count == k) {//加入的是已经有的元素，或者是第一次有k个元素
                while (num[arr[left1]] - 1 >= 1) {//重复元素，可以向右缩进
                    left2++;
                    num[arr[left1]]--;
                    left1++;
                }

            } else {//新元素加入右边界
                while (num[arr[left1]] - 1 >= 1) {//重复元素，可以向右缩进
                    num[arr[left1]]--;
                    left1++;
                }

                //左边界向右移除一个元素
                num[arr[left1]]--;
                left1++;
                left2 = 1;//新元素相当与设置了新的左边界，所以重置缩进量为1
                count--;

                //尝试继续向右缩进
                while (num[arr[left1]] - 1 >= 1) {
                    left2++;
                    num[arr[left1]]--;
                    left1++;
                }
            }

            result += left2;
            r++;
        }


        return result;
    }

}
