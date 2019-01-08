import org.junit.Test;

/**
 * Created by 01369755 on 2018/7/6.
 */
public class test {
    @Test
    public void test1(){
        String request = "POST /test HTTP/1.1\n" +
                "Connection: keep-alive\n" +
                "Content-Type: application/x-www-form-urlencoded\n" +
                "Content-Length: 8\n" +
                "Host: 10.118.59.35:8888\n" +
                "User-Agent: Apache-HttpClient/4.5.5 (Java/1.8.0_131)\n" +
                "test=123";

        String request1 = "POST /test/123 HTTP/1.1Connection: keep-aliveContent-Type: application/x-www-form-urlencodedContent-Length: 8Host: 10.118.59.35:8888User-Agent: Apache-HttpClient/4.5.5 (Java/1.8.0_131)test=123";
        String[] requestArray = request.split("\n");
        for (String s : requestArray){
            System.out.println(request1.contains(s));
        }
//        System.out.println(request.substring(request.indexOf("Content-Length:"),request.indexOf("\n")));
    }
}
