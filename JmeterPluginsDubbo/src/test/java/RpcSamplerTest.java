import org.I0Itec.zkclient.IZkStateListener;
import com.alibaba.dubbo.config.RegistryConfig;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import org.zys.jmeter.protocol.rpc.sampler.util.ObjectMapperUtil;

/**
 * Created by 01369755 on 2018/3/14.
 */
public class RpcSamplerTest {

   /* public static String NAME = "RPC请求";
    public static String ZOOKEEPER = "ZK地址";
    public static String INTERFACE = "接口名";
    public static String METHOD_NAME = "方法名称";
    public static String METHOD_INDEX = "方法索引";
    public static String VERSION = "版本";
    public static String ARGS = "请求参数";*/

    public static ApplicationConfig RPCSAMPLER = new ApplicationConfig("rpcSampler");

    public static final Logger log = LoggerFactory.getLogger(RpcSamplerTest.class);

    private String zookeeper = "zookeeper://10.202.35.91:2181";
    private String interfaceName = "com.sf.o2o.dds.pms.service.FeederBuildBagCfgService";
    private int    methodIndex = 1;
    private String version = "3.0beta";
    private String args = "{\"systemCode\": \"DDS_TAS\",\"cityCode\": \"755\",\"bagSrcDeptCode\": \"755AT\",\"bagDestDeptCode\": \"755T\",\"operateEmpCode\": \"D739231N\",\"operateDeptCode\": \"021FB\",\"operateTm\": \"2017-12-12 11:01:45\"}";

   /* private String getZookeeper()
    {
        return getPropertyAsString(ZOOKEEPER);
    }

    private String getInterfaceName() {
        return getPropertyAsString(INTERFACE);
    }

    private int getMethodIndex() {
        return getPropertyAsInt(METHOD_INDEX);
    }

    private String getVersion() {
        return getPropertyAsString(VERSION);
    }

    private String getArgs() {
        return getPropertyAsString(ARGS);
    }
    public SampleResult sample(Entry entry)
    {
        SampleResult res = new SampleResult();
        res.setSampleLabel(getName());
        zookeeper = getZookeeper().trim();
        interfaceName = getInterfaceName().trim();
        methodIndex = getMethodIndex();
        version = getVersion().trim();
        args = getArgs().trim().replace("\n", "").replace("\t", "");
        try
        {
            res.sampleStart();
            res.setResponseData(visitRpcService(), "UTF-8");
            res.setSuccessful(true);
        } catch (Exception e) {
            res.setResponseCode("500");
            res.setResponseData(e.toString(), "UTF-8");
            res.setSuccessful(false);
            res.setResponseMessage("找不到服务或请求参数不匹配");
            return res;
        } finally {
            res.sampleEnd();
        }
        return res;
    }
*/
   @Test
    public void visitRpcService() throws InvocationTargetException, IllegalAccessException, IOException, ClassNotFoundException {

        Class interfaceClass = Class.forName(interfaceName);
        ReferenceConfig ref = ReferenceConfigSinglet.getReference(zookeeper, interfaceName, version);
        Method method = interfaceClass.getMethods()[methodIndex];
        Class<?>[] paramTypes = method.getParameterTypes();
        System.out.print(ObjectMapperUtil.objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(method.invoke(ref.get(), ObjectMapperUtil.objectMapper.readValue(args,paramTypes[0]))));
    }

    private static class ReferenceConfigSinglet {

        private static ConcurrentHashMap<String, ReferenceConfig> clients = new ConcurrentHashMap<String, ReferenceConfig>();

        private ReferenceConfigSinglet() {
        }

        private static ReferenceConfig getReference(String zk, String cls, String version) throws IOException {
            String key = zk + cls + version;
            if (!clients.containsKey(key)) {
                ReferenceConfig ref = new ReferenceConfig();
                RegistryConfig registryConfig = new RegistryConfig(zk);
                registryConfig.setId(key);
                ref.setRegistry(registryConfig);
                ref.setApplication(RPCSAMPLER);
                ref.setInterface(cls);
                ref.setRetries(0);
                ref.setVersion(version);
                clients.put(key, ref);
            }
            return clients.get(key);
        }
    }
}
