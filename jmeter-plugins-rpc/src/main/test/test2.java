import com.alibaba.dubbo.common.URL;
import com.google.gson.Gson;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Created by 01369755 on 2018/9/13.
 */
public class test2 {
    @Test
    public void test() {
        int[] a = {};
        StringBuilder methodName = new StringBuilder("a").append('(');

        for (int pt : a) {
            methodName.append(pt).append(',');
        }
        methodName.deleteCharAt(methodName.lastIndexOf(","));
        methodName.append(')');
        System.out.println(methodName.toString());
    }

    @Test
    public void test2() {
//        boolean a = false;
//        System.out.println(a);
        String a = "(,)";
        ;
        System.out.println(StringUtils.containsNone(a, ","));
    }


    class Solution {
        public int[] twoSum(int[] nums, int target) {
            for (int i = 0; i < nums.length - 1; i++) {
                for (int j = i + 1; j < nums.length; j++) {
                    if (nums[i] + nums[j] == target) {
                        return new int[]{i, j};
                    }
                }
            }
            return null;
        }
    }

    @Test
    public void test3(){
        Class c = null;
        try {
            c = Class.forName("java.lang.reflect.Method");
            for (Method m : c.getDeclaredMethods()){
                if ("invoke".equals(m.getName())){
                    System.out.println(m.toGenericString());
                    System.out.println(m.toString());
                    for (Class p : m.getParameterTypes()){
                        System.out.println(p.getSimpleName());
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test4(){
        String a = "[1,2,3]";
        int[] m = new Gson().fromJson(a,int[].class);
        System.out.println(m.length);
    }

    @Test
    public void test5(){
        URL url = new URL("dubbo","127.0.0.2",20941,"com.sf.o2o.dds.pms.service.QueryBagService.queryBagForBagNo");
        System.out.println(url.toIdentityString());
    }

    @Test
    public void test6(){
        String[] strings = ArrayUtils.EMPTY_STRING_ARRAY;
        for (String s : strings){
            System.out.println(s);
        }
    }
}
