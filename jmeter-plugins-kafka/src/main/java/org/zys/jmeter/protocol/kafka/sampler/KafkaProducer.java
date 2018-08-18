package org.zys.jmeter.protocol.kafka.sampler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kafka.producer.KeyedMessage;
import org.apache.commons.codec.Charsets;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.kafka.config.KafkaProperty;
import org.zys.jmeter.protocol.kafka.utils.ProtostuffRuntimeUtil;

/**
 * Created by zhuyongsheng on 2018/3/22.
 */
public class KafkaProducer extends AbstractSampler implements TestBean{

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private static Gson GSON = new GsonBuilder().setPrettyPrinting().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private String topic;
    private String message;
    private String key;

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSamplerData(topic + "\n" + message);
        result.setDataType("text");
        result.sampleStart();
        try {
            run();
            result.setResponseData("message sent successfully.", "utf8");
            result.setSuccessful(true);
            result.setResponseCode("0");
            result.setResponseMessage("OK");
        } catch (Exception e) {
            e.printStackTrace();
            result.setResponseData(e.toString(), "utf8");
            result.setSuccessful(false);
            result.setResponseCode("500");
            result.setResponseMessage("KO");
        }
        result.sampleEnd();
        return result;
    }

    private void run() throws Exception {
        KafkaProperty kafkaProperty = (KafkaProperty)getProperty(topic).getObjectValue();
        KeyedMessage<byte[], byte[]> msg;
        message = message.trim().replace("\n", "").replace("\t","");
        Class clazz = kafkaProperty.getSerializeClazz();
        if (null == clazz){
            msg = new KeyedMessage(topic, key.getBytes(Charsets.UTF_8), message.getBytes(Charsets.UTF_8));
        }else {
            msg = new KeyedMessage(topic, key.getBytes(Charsets.UTF_8), ProtostuffRuntimeUtil.serialize(GSON.fromJson(message, clazz)));
        }
        kafkaProperty.getProducer().send(msg);
    }


    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
