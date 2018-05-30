import org.apache.jorphan.reflect.ClassFilter;
import org.apache.jorphan.reflect.ClassFinder;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

/**
 * Created by 01369755 on 2018/4/24.
 */
public class test {

    @Test
    public void printclassName(){
        try {
            System.setProperty("java.class.path", System.getProperties()+";"+"D:\\Program Files\\apache-jmeter-3.2\\lib\\dubbo\\o2o-dds-pms-client-1.2.5-SNAPSHOT.jar");
            List<String> cls= ClassFinder.findClasses(new String[]{"D:\\Program Files\\apache-jmeter-3.2\\lib\\dubbo\\o2o-dds-pms-client-1.2.5-SNAPSHOT.jar"},
                    new InterfaceFilter());
            System.out.println(cls.size());
            for (String c : cls){
                System.out.println(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static class InterfaceFilter implements ClassFilter {
        private final ClassLoader contextClassLoader
                = Thread.currentThread().getContextClassLoader();
        InterfaceFilter() {
        }

        @Override
        public boolean accept(String className) {
            try {
                if (Class.forName(className, false, contextClassLoader).isInterface()) {
                    return true;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return true;
        }
    }
}
