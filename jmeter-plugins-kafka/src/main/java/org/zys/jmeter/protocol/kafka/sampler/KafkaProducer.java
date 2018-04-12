package org.zys.jmeter.protocol.kafka.sampler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sf.fvp.ConvertUtil;
import com.sf.fvp.dto.FactRouteDto;
import kafka.producer.KeyedMessage;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.kafka.config.KafkaConfig;
import java.text.SimpleDateFormat;

/**
 * Created by 01369755 on 2018/3/22.
 */
public class KafkaProducer extends AbstractSampler implements TestBean{

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    public static final ObjectMapper OBJECT_MAPPER  = new ObjectMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    private String topic;
    private String serializer;
    private String message;

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

    private void run() throws Exception {
        KeyedMessage<String, byte[]> msg;
        message = message.trim().replace("\n", "").replace("\t","");
        switch (serializer) {
            case "STRING":
                msg = new KeyedMessage(topic, message.getBytes("UTF-8"));
                break;
            case "FACTROUTEDTO":
                msg = new KeyedMessage(topic, ConvertUtil.toByte(OBJECT_MAPPER.readValue(message, FactRouteDto.class)));
                break;
            default:
                throw new Exception("unsupported serializer.");
        }
        KafkaConfig.getProducer(topic).send(msg);
    }


    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getSerializer() {
        return serializer;
    }

    public void setSerializer(String serializer) {
        this.serializer = serializer;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
