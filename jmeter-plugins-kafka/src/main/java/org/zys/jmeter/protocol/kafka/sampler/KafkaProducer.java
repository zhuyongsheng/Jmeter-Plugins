package org.zys.jmeter.protocol.kafka.sampler;

import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.kafka.config.KafkaProperty;

/**
 * Created by zhuyongsheng on 2018/3/22.
 */
public class KafkaProducer extends AbstractSampler implements TestBean {

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);

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
            ((KafkaProperty) getProperty(topic).getObjectValue()).produce(key, message);
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
