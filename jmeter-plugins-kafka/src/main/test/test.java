import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

/**
 * Created by 01369755 on 2018/7/30.
 */
public class test {

    @Test
    public void test1() {
        int[] a = {1, 2, 3};
        int n = a[2];
        n = 4;
        System.out.println(a[2]);
        /*int i = 0;
        int j = 0;
        while (j<35) {
            System.out.println(i++ % 32);
            j++;
        }*/
        /*int i = 0;
        for(int j=0; j<20; j++){
            System.out.println(++i % 5);
        }*/
//        System.out.println(System.getProperty("java.util.concurrent.ForkJoinPool.common.parallelism"));

//        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "32");
        List<String> stringList = new ArrayList<>();
        for (int i = 1; i <= 32; i++) {
            stringList.add(String.valueOf(i));
        }
        AtomicBoolean big = new AtomicBoolean(false);
        StringBuilder sb = new StringBuilder();
        stringList.parallelStream().forEach((s) -> {
            System.out.println(s);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            /*while (!big.get()) {
                double d = Math.random() * 10;
                System.out.println(s + ":" + d);

                if (d >= 9.9) {
                    big.set(true);
                    sb.append(s).append(":").append(d);
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/
        });

//        System.out.println(sb.toString());

//        Gson gson = new Gson();
//        String s = "{\"requestId\":null,\"success\":true,\"business\":null,\"errorCode\":null,\"errorMessage\":null,\"date\":null,\"version\":null,\"obj\":{\"containerNo\":\"984363473846\",\"containerType\":\"C1\",\"srcDeptCode\":\"539A\",\"destDeptCode\":\"571U\",\"limitTypeCode\":null,\"weight\":null,\"status\":\"100000\",\"operateTm\":\"2018-08-02 19:01:33\",\"higherContainerNo\":null,\"extend\":null,\"createTm\":\"2018-08-02 19:01:33\",\"createEmpCode\":\"982864\",\"destUnitAreaCode\":null,\"sysSource\":\"NEXT-TCS-SERVER\",\"virtualFlag\":\"2\",\"boardNo\":null,\"gridPortNo\":null,\"networkType\":\"2\",\"bizFlag\":null,\"packageList\":[{\"packageNo\":\"847489150702\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"779354617954\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"821606193466\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"326407384099\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"258911340953\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"769676105986\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"755939560838\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"730827596827\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"731799903747\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"571349610725\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"874914923392\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"843132695940\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"844368349736\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"845327859252\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"846223553610\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"452106659873\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"563108790674\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"862045447519\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"002601025971\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"},{\"packageNo\":\"965685188977\",\"createTm\":\"2018-08-02 19:01:33\",\"relCreateTm\":\"2018-08-02 19:01:33\"}],\"containerHigherList\":[],\"containerStatusRecordList\":[{\"status\":\"100000\",\"createTm\":\"2018-08-02 19:01:33\",\"containerNo\":\"984363473846\",\"higherContainerNo\":null,\"createEmpCode\":\"982864\",\"operateTm\":\"2018-08-02 19:01:33\"}],\"cityCode\":\"539\",\"mainWaybillNo\":null}}";
//        for (String s1 : StringUtils.substringsBetween(s,"\"packageNo\":\"","\",")){
//            System.out.println(s1);
//        }
        /*for (String s : stringList) {
            System.out.println(s);
            if (s.equals("2")) {
                return;
            }
        }*/
        /*String s = "{\"id\":\"vRCdGTHtR8me9u7XA4gBVg\",\"packageNo\":\"755081819682\",\"packageStatus\":\"10\",\"eventCode\":\"10_1061\",\"operateCode\":\"51\",\"operateTm\":\"2018-08-18 17:22:38,000\",\"operateEmpCode\":\"000212\",\"operateZoneCode\":\"755A\",\"sysSource\":\"FVP-CORE\",\"createTm\":\"2018-08-18 17:22:42,414\"}";
        Gson gson = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        JsonParser jp = new JsonParser();
        System.out.println(gson.toJson(jp.parse(s)));*/
//        StringBuilder sb = new StringBuilder();
//        System.out.println(sb.length());
//        List<String> stringList;
//        Byte[] bs = {69,68};
//        for (Byte c : bs){
//            System.out.print(String.valueOf(c));
//        }
        /*String seed = "10.0.0.1:9002";
        System.out.println(StringUtils.substringBefore(seed, ":"));
        System.out.println(StringUtils.substringAfter(seed, ":"));*/

    }

    public static void test(String s) {
        Gson gson = new Gson();
        int num = gson.fromJson(s, int.class);
        System.out.println(gson.hashCode());
    }

    @Test
    public void test2() {
        int[] a = {};
        for (int i : a) {
            System.out.println("in");
        }
    }

    @Test
    public void test3() {
        int i = 0, j = 32;
        for (int k = 0; k < 100; k++) {
            System.out.println(i++ & (j - 1));
        }
    }
}
