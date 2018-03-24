import org.junit.Test;

import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * Created by 01369755 on 2018/1/4.
 */
public class test {
//    @Test
    public void xxx(){
//        System.out.println("123");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String command = scanner.next();
            System.out.println(command);
            if (command.equals("exit")) break; // 这行代码表示如果输入的是exit，则退出循环；if和break马上就会讲解
        }
        scanner.close();
    }
    public static void main(String[] args) {

        System.out.println(Charset.defaultCharset().name());
    }
}
