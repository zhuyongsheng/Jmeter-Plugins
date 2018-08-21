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
public class KafkaConsumer extends AbstractSampler implements TestBean {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);


    private String topic;
    private int duration;
    private String wanted;

    public SampleResult sample(Entry entry) {
        SampleResult res = new SampleResult();
        res.setSamplerData("Fetch " + wanted + " in " + topic);
        res.setSampleLabel(getName());
        res.sampleStart();
        try {

            res.setResponseData(((KafkaProperty) getProperty(topic).getObjectValue()).consume(wanted, duration), "UTF-8");
            res.setResponseCode("0");
            res.setSuccessful(true);

        } catch (Exception e) {
            e.printStackTrace();
            res.setResponseMessage(e.getMessage());
            res.setResponseCode("500");
            res.setSuccessful(false);
        }
        res.sampleEnd();
        return res;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getWanted() {
        return wanted;
    }

    public void setWanted(String wanted) {
        this.wanted = wanted;
    }

}
