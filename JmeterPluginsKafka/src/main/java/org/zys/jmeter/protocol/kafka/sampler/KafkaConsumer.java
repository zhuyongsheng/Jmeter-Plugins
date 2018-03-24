package org.zys.jmeter.protocol.kafka.sampler;

import kafka.api.FetchRequest;
import kafka.api.FetchRequestBuilder;
import kafka.api.PartitionOffsetRequestInfo;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.message.MessageAndOffset;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.apache.jmeter.util.JMeterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zys.jmeter.protocol.kafka.config.KafkaConfig;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static kafka.api.OffsetRequest.CurrentVersion;
import static kafka.api.OffsetRequest.EarliestTime;
/**
 * Created by 01369755 on 2018/3/22.
 */
public class KafkaConsumer extends AbstractSampler implements TestBean {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);

    private final static int FETCH_SIZE = 100000;

    private String topic;
    private int duration;
    private String wanted;


    public SampleResult sample(Entry entry) {
        SampleResult res = new SampleResult();
        StringBuffer sp = new StringBuffer("Fetch " + wanted + " in " + topic);
        res.setSamplerData(sp.toString());
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
        }
        return res;
    }
    private static Boolean isMatch(String msg, String[] wants){
        for (String w : wants){
            if (!msg.contains(w)){
                return false;
            }
        }
        return true;
    }

    public String run() throws InterruptedException {
        SimpleConsumer[] simpleConsumers = KafkaConfig.consumerMap.get(topic);
        Long[] offsets = KafkaConfig.offsetMap.get(topic);
        int partitionNum = offsets.length;
        ExecutorService executor = Executors.newFixedThreadPool(partitionNum);

        StringBuilder sb = new StringBuilder();
        AtomicBoolean isCaught = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(partitionNum);
        long beginTime = System.currentTimeMillis();
        for (int partition = 0; partition < partitionNum; partition++) {
            final int a_partition = partition;
            executor.execute(new Runnable() {
                @Override
                public void run() {

                    while (!isCaught.get() && System.currentTimeMillis() - beginTime < duration) {
                        try {
                            FetchRequest req = new FetchRequestBuilder()
                                    .clientId(simpleConsumers[a_partition].clientId())
                                    .addFetch(topic, a_partition, offsets[a_partition], FETCH_SIZE) // Note: this fetchSize of 100000 might need to be increased if large batches are written to Kafka
                                    .build();
                            FetchResponse fetchResponse = simpleConsumers[a_partition].fetch(req);
                            for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(topic, a_partition)) {
                                long currentOffset = messageAndOffset.offset();
                                if (currentOffset < offsets[a_partition]) {
                                    continue;
                                }
                                offsets[a_partition] = messageAndOffset.nextOffset();
                                ByteBuffer payload = messageAndOffset.message().payload();
                                byte[] bytes = new byte[payload.limit()];
                                payload.get(bytes);
                                String msg = new String(bytes, "UTF-8");
                                if (isMatch(msg,wanted.split(","))) {
                                    isCaught.set(true);
                                    sb.append(a_partition + "_" + String.valueOf(currentOffset) + ": \n" + msg);
                                }
                            }
                        } catch (Exception e) {
                        }
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdownNow();

        KafkaConfig.offsetMap.put(topic, offsets);

        if (sb.length() > 0) {
            return sb.toString();
        }
        return "message match failed.";
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
