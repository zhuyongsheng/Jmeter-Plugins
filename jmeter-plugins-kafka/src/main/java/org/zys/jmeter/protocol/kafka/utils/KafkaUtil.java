package org.zys.jmeter.protocol.kafka.utils;

import kafka.api.PartitionOffsetRequestInfo;
import kafka.cluster.Broker;
import kafka.common.TopicAndPartition;
import kafka.javaapi.*;
import kafka.javaapi.consumer.SimpleConsumer;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;

import java.util.*;

import static kafka.api.OffsetRequest.CurrentVersion;
import static kafka.api.OffsetRequest.LatestTime;

/**
 * Created by zhuyongsheng on 2018/6/20.
 */
public class KafkaUtil {

    private final static int TIME_OUT = 100000;
    private final static int BUFFER_SIZE = 64 * 1024;

    public static Producer initProducer(String brokers) {
        Properties props = new Properties();
        props.put("metadata.broker.list", brokers);
        ProducerConfig config = new ProducerConfig(props);
        return new Producer(config);
    }

    public static List<SimpleConsumer> initConsumer(int partitionNum, String brokers, String topicName) {
        List<SimpleConsumer> simpleConsumerList = new ArrayList<>();
        for (int partition = 0; partition < partitionNum; partition++) {
            Broker b = findLeader(brokers, topicName, partition);
            simpleConsumerList.add(new SimpleConsumer(b.host(), b.port(), TIME_OUT, BUFFER_SIZE, String.valueOf(partition)));
        }
        return simpleConsumerList;
    }

    public static long[] initOffset(List<SimpleConsumer> simpleConsumerList, String topicName) {
        long[] offsets = new long[simpleConsumerList.size()];
        simpleConsumerList.forEach(simpleConsumer -> {
            int partition = Integer.parseInt(simpleConsumer.clientId());
            offsets[partition] = getLastOffset(simpleConsumer, topicName, partition);
        });
        return offsets;
    }

    private static Broker findLeader(String brokers, String topic, int a_partition) {
        List<String> Brokers = new ArrayList<>(Arrays.asList(brokers.split(",")));
        PartitionMetadata returnMetaData = null;
        loop:
        for (String seed : Brokers) {
            SimpleConsumer consumer = null;
            try {
                String[] seedInfo = seed.split(":");
                consumer = new SimpleConsumer(seedInfo[0], Integer.parseInt(seedInfo[1]), TIME_OUT, BUFFER_SIZE, "leaderLookup");
                List<String> topics = Collections.singletonList(topic);
                TopicMetadataRequest req = new TopicMetadataRequest(topics);
                TopicMetadataResponse resp = consumer.send(req);

                List<TopicMetadata> metaData = resp.topicsMetadata();
                for (TopicMetadata item : metaData) {
                    for (PartitionMetadata part : item.partitionsMetadata()) {
                        if (part.partitionId() == a_partition) {
                            returnMetaData = part;
                            break loop;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error communicating with Broker [" + seed + "] to find Leader for [" + topic
                        + ", " + a_partition + "] Reason: " + e);
            } finally {
                if (consumer != null) consumer.close();
            }
        }
        return returnMetaData.leader();
    }


    private static long getLastOffset(SimpleConsumer consumer, String topicName, int partition) {
        TopicAndPartition topicAndPartition = new TopicAndPartition(topicName, partition);
        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(LatestTime(), 1));
        OffsetResponse response = consumer.getOffsetsBefore(new OffsetRequest(requestInfo, CurrentVersion(), consumer.clientId()));

        if (response.hasError()) {
            System.out.println("Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topicName, partition));
            return 0;
        }
        long[] offsets = response.offsets(topicName, partition);
        if (offsets.length > 0) {
            return offsets[0];
        }
        return 0;
    }
}
