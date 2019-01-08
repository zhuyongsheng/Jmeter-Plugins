import org.junit.Test;
import redis.clients.jedis.JedisPoolConfig;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuyongsheng on 2018/3/21.
 */
public class test {
    private static JedisPoolConfig CONFIG = new JedisPoolConfig();
    @Test
    public void test(){
        System.out.println(CONFIG.getMaxIdle());
    }

    private Map<String,Integer> map = new HashMap();

    @Test
    public void test1(){
        Integer a = 1;
        try {
            Method method = Integer.class.getDeclaredMethod("equals",Object.class);
            System.out.println(method.invoke(a,1));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

//                StringBuilder sb = new StringBuilder();
//        System.out.println(0 == sb.length());
//        List<String> stringList = new ArrayList<>();
//        stringList.forEach(s -> {System.out.println(s+"1");});
//        Integer s = 1;
//        map.put("key",s);
//        s = 3;
//        System.out.println(map.get("key"));

        /*int[] a = {1,2};
        map.put("key",a);
        a[1] = 3;
        for (int i : map.get("key")){

            System.out.println(i);
        }*/
    }
}
