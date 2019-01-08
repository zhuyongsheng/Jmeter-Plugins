/**
 * Created by 01369755 on 2018/9/13.
 */
public class FillWater {
    /**
     * 问题抽象：
     * 使用x升和y升的水杯组合出z升的水。(x、y、z都为正整数)
     * 如果案例有解，那么应该有 ax + by = z (a，b为整数)的解。
     * 想想，如果以上等式无解，那么是组合不出z升水的，因为 组合 的方式是倒满小的水杯，
     * 小的水杯再全部倒进大的水杯，再倒满小水杯这样直到大水杯满了，将大水杯倒掉，然后将
     * 小水杯的水都倒进大水杯，然后再装满小水杯，小水杯再装满大水杯，这一系列反复的过程
     * 都可以看到，到给小水杯的水是整数倍，而从大水杯倒出去的水也是整数倍，所以以上方程
     * 必须要有整数解。
     */
    public static void main(String[] args) {
        startFillWater(9, 4, 6);
    }

    /**
     * 开始装水
     *
     * @param x 小水杯容量
     * @param y 大水杯容量
     * @param z 要得到的容量
     */
    public static void startFillWater(int x, int y, int z) {
        if (x + y < z || z < 0) {
            System.out.println("此题无解....");
            return;
        }
        if (x > y) {
            int _t = x;
            x = y;
            y = _t;
        }
        // 定义大小水杯中已装的水量
        System.out.println("开始装水......");
        int a = 0, b = 0;
        if (y - x == z) {
            // 先倒入大水杯
            System.out.println("大水杯倒满" + x + "升水!");
            b = y;
            // 再用大水杯将小水杯倒满，大水杯余下的就是z
            System.out.println("用大水杯的水倒满小水杯!");
            b = y - a;
            System.out.println("OK！现在大杯中有 " + z + " 升水");
            return;
        }
        // 先将小水杯装满
        System.out.println("小水杯倒满" + x + "升水!");
        a = x;
        // 将小水杯水倒入大水杯
        System.out.println("将小水杯倒" + x + "升水到大水杯!");
        b = a;
        a = 0;
        System.out.println("现在水量：" + "小(" + a + "), 大(" + b + ")");
        int i = 0;
        while (a != z) {
            if (a != 0) {
                System.out.println("将小水杯倒" + a + "升水到大水杯!");
                b = a;
                a = 0;
            }
            System.out.println("小水杯倒满" + x + "升水!");
            a = x;
            System.out.println("将小水杯的水慢慢倒入到大水杯!");
            if (a + b > y) {
                int temp = y - b;
                System.out.println("大水杯满了，清空它");
                b = 0;
                a = a - temp;
            } else {
                b = b + a;
                a = 0;
            }
            System.out.println("现在水量：" + "小(" + a + "), 大(" + b + ")");
            if (a == z || b == z) {
                return;
            }
            if (i++ >= 1000) {
                System.out.println("已经尝试了1000次，此题可能无解！");
                return;
            }
        }
        System.out.println("OK！现在小杯中有 " + z + " 升水");
    }
}
