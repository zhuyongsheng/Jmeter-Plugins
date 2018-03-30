package org.zys.jmeter.protocol.kafka.sampler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sf.fvp.ConvertUtil;
import com.sf.fvp.dto.FactRouteDto;
import kafka.producer.KeyedMessage;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.kafka.config.KafkaConfig;
import java.text.SimpleDateFormat;

/**
 * Created by 01369755 on 2018/3/22.
 */
public class KafkaProducer extends AbstractSampler{

    private static final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    public static final ObjectMapper OBJECT_MAPPER  = new ObjectMapper().setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    public static String[] SERIALIZE_TYPE = {"STRING", "FACTROUTEDTO"};

    public static String TOPIC = "topic";
    public static String SERIALIZER = "serializer";
    public static String MESSAGE = "message";

    private String topic;
    private String serializer;
    private String message;

    private void init(){
        topic = getPropertyAsString(TOPIC);
        serializer = getPropertyAsString(SERIALIZER);
        message = getPropertyAsString(MESSAGE).trim().replace("\n", "").replace("\t", "");
    }

    @Override
    public SampleResult sample(Entry entry) {
        SampleResult result = new SampleResult();
        result.setSampleLabel(getName());
        result.setSamplerData(message);
        result.setDataType("text");
        result.sampleStart();
        try {
            init();
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
        KafkaConfig.producerMap.get(topic).send(msg);
    }
}
