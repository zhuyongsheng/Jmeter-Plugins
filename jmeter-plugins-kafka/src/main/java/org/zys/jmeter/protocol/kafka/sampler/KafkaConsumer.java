package org.zys.jmeter.protocol.kafka.sampler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import kafka.api.FetchRequestBuilder;
import kafka.javaapi.consumer.SimpleConsumer;
import org.apache.commons.codec.Charsets;
import org.apache.commons.lang.BooleanUtils;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.threads.JMeterVariables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.kafka.config.KafkaEntity;
import org.zys.jmeter.protocol.kafka.utils.ProtostuffRuntimeUtil;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by zhuyongsheng on 2018/3/22.
 */
public class KafkaConsumer extends AbstractSampler implements TestBean {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    private final static Gson GSON = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setPrettyPrinting().create();
    private final static ExecutorService executor = Executors.newCachedThreadPool();

    private String topic;
    private int duration;
    private String wanted;

    public SampleResult sample(Entry entry) {
        SampleResult res = new SampleResult();
        StringBuffer samplerDate = new StringBuffer("Fetch " + wanted + " in " + topic);
        res.setSamplerData(samplerDate.toString());
        res.setSampleLabel(getName());
        res.sampleStart();
        try {
            res.setResponseData(run(), "UTF-8");
            res.setResponseCode("0");
            res.setSuccessful(true);

        } catch (Exception e) {
            e.printStackTrace();
            res.setResponseMessage(e.getMessage());
            res.setResponseCode("500");
            res.setSuccessful(false);
        } finally {
            res.sampleEnd();
            return res;
        }
    }

    private Boolean isMatch(String msg, String wanted) {
        String[] wants = wanted.split(",");
        for (String w : wants) {
            if (!msg.contains(w)) {
                return false;
            }
        }
        return true;
    }

    private String run() throws InterruptedException {
        KafkaEntity kafkaEntity = (KafkaEntity) getProperty(topic).getObjectValue();
        Class clazz = kafkaEntity.getSerializeClazz();
        List<SimpleConsumer> simpleConsumerlist = kafkaEntity.getSimpleConsumerList();
        JMeterVariables variables = getThreadContext().getVariables();
        Object object = variables.getObject(topic);
        if (null == object) {
            object = kafkaEntity.getOffsets().clone();
            variables.putObject(topic, object);
        }
        long[] offsets = (long[]) object;
        CountDownLatch latch = new CountDownLatch(simpleConsumerlist.size());
        StringBuilder sb = new StringBuilder();
        AtomicBoolean isCaught = new AtomicBoolean(false);
        long beginTime = System.currentTimeMillis();
        simpleConsumerlist.forEach(simpleConsumer -> {
            int partition = Integer.parseInt(simpleConsumer.clientId());
            executor.execute((Runnable) () -> {
                while (BooleanUtils.isFalse(isCaught.get()) && System.currentTimeMillis() - beginTime < duration) {
                    try {
                        simpleConsumer.fetch(
                                new FetchRequestBuilder().addFetch(topic, partition, offsets[partition], simpleConsumer.bufferSize()).build()
                        ).messageSet(topic, partition).forEach(messageAndOffset -> {
                            if (messageAndOffset.offset() >= offsets[partition]) {
                                offsets[partition] = messageAndOffset.nextOffset();
                                ByteBuffer payload = messageAndOffset.message().payload();
                                byte[] bytes = new byte[payload.limit()];
                                payload.get(bytes);
                                String msg;
                                if (null == clazz) {
                                    msg = new String(bytes, Charsets.UTF_8);
                                } else {
                                    msg = GSON.toJson(ProtostuffRuntimeUtil.deserialize(bytes, clazz));
                                }
                                if (isMatch(msg, wanted)) {
                                    sb.append("{\"partition\":\"").append(partition).append("\",")
                                            .append("\"offset\":\"").append(String.valueOf(messageAndOffset.offset())).append("\",")
                                            .append("\"message\":").append(msg).append("}\n");
                                    isCaught.set(true);
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                latch.countDown();
            });
        });
        latch.await();
        if (sb.length() > 0) {
            return sb.deleteCharAt(sb.lastIndexOf("\n")).toString();
        }
        return "message not found.";
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
