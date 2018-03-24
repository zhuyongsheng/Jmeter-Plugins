package org.zys.jmeter.protocol.kafka.sampler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sf.fvp.ConvertUtil;
import com.sf.fvp.dto.FactRouteDto;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.kafka.config.KafkaConfig;

import java.io.IOException;

/**
 * Created by 01369755 on 2018/3/22.
 */
public class KafkaProducer extends AbstractSampler implements TestBean{

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private String topic;
    private String message;
    private String serializer;

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSamplerData(message);
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

    private void run() throws IOException {
        KeyedMessage<String, byte[]> msg;
        switch (serializer) {
            case "STRING":
                msg = new KeyedMessage(topic, message.getBytes("UTF-8"));
                break;
            case "FVP(PROTOSTUFF)":
                msg = new KeyedMessage(topic, ConvertUtil.toByte(gson.fromJson(message, FactRouteDto.class)));
                break;
            default:
                throw new UnsupportedOperationException("不支持的序列化类型");
        }
        KafkaConfig.producerMap.get(topic).send(msg);
    }
    public String getSerializer() {
        return serializer;
    }

    public void setSerializer(String serializer) {
        this.serializer = serializer;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message.trim().replace("\n", "").replace("\t", "");
    }

}
