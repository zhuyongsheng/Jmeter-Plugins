import org.junit.Test;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by 01369755 on 2018/3/21.
 */
public class test {
    private static JedisPoolConfig CONFIG = new JedisPoolConfig();
    @Test
    public void test(){
        System.out.println(CONFIG.getMaxIdle());
    }
}
