import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by 01369755 on 2018/8/14.
 */
public class test1 {
    private static final long count = 2000000000l;

    public static void main(String[] args) throws Exception {
        concurrency();
        serial();
    }

    private static void concurrency() throws Exception {
        long start = System.currentTimeMillis();
        Thread thread = new Thread((Runnable) () -> {
            int a = 0;
            for (int i = 0; i < count; i++) {
                a += 5;
            }
        });
        thread.start();
        int b = 0;
        for (long i = 0; i < count; i++) {
            b--;
        }
        thread.join();
        long time = System.currentTimeMillis() - start;
        System.out.println("Concurrency：" + time + "ms, b = " + b);
    }

    private static void serial() {
        long start = System.currentTimeMillis();
        int a = 0;
        for (long i = 0; i < count; i++) {
            a += 5;
        }
        int b = 0;
        for (int i = 0; i < count; i++) {
            b--;
        }
        long time = System.currentTimeMillis() - start;
        System.out.println("Serial：" + time + "ms, b = " + b + ", a = " + a);
    }

    @Test
    public void test(){
        List<String> nsTypeCodeList = Arrays.asList("CONTAINER_01_000","WAYBILL_01_000","WAYBILL_03_000");
        System.out.println(nsTypeCodeList.size());
    }
}