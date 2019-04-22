package xin.awell.dt.client.core;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import redis.clients.jedis.Jedis;
import xin.awell.dt.client.utils.RedisPool;
import xin.awell.dt.core.constant.JobPriority;
import xin.awell.dt.core.constant.JobType;
import xin.awell.dt.core.domain.JobInstance;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author lzp
 * @since 2019/2/260:43
 */
@Slf4j
public class JobInstanceChannel {

    private static HashMap<String, JobInstanceChannel> jobInstanceChannelHashMap = new HashMap<>();

    private static String INSTANCE_QUEUE_PREFIX = "dt-job-instance-";

    private String appId;
    private String instanceId;
    private BlockingQueue<JobInstance> localRetryQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<JobInstance> commonJobInstanceQueue = new LinkedBlockingQueue<>();
    private DefaultMQProducer mqProducer;
    private long consumeTime;

    public boolean sendJobInstance(JobInstance jobInstance) {
        if (jobInstance.getJobType() == JobType.commonJob) {
            Message msg = new Message("dt",
                    appId,
                    jobInstance.getInstanceId(),
                    JSON.toJSONBytes(jobInstance));
            try {
                SendResult sendResult = mqProducer.send(msg);
                if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        } else {
            String queueName = INSTANCE_QUEUE_PREFIX + appId + "-queues-" + jobInstance.getPriority();
            try (Jedis jedis = RedisPool.getInstance()) {
                Long result = jedis.lpush(queueName, JSON.toJSONString(jobInstance));
                System.out.println("send! " + result);
            }
            return true;
        }
    }

    public boolean sendJobInstanceList(List<JobInstance> instanceList) {
        instanceList.forEach(this::sendJobInstance);
        return true;
    }

    private JobPriority getTargetPriority() {
        double random = Math.random();

        //p1 p2 p3 p4 :: 4:3:2:1
        if (random < 0.1) {
            return JobPriority.p4;
        } else if (random < 0.3) {
            return JobPriority.p3;
        } else if (random < 0.6) {
            return JobPriority.p2;
        } else {
            return JobPriority.p1;
        }
    }

    public JobInstance getJobInstance() {
        /*
        可能在同步之前就localQueue就空了， 那么取分布式队列的
        循环直到真正取出了一个任务后， 取不到任务， 表示目标队列可能是空的
         */
        while (true) {
            if (!localRetryQueue.isEmpty()) {
                JobInstance jobInstance = localRetryQueue.poll();
                if (jobInstance != null) {
                    return jobInstance;
                }
            }

            if (!commonJobInstanceQueue.isEmpty()) {
                JobInstance jobInstance = commonJobInstanceQueue.poll();
                if (jobInstance != null) {
                    return jobInstance;
                }
            }

            JobPriority priority = getTargetPriority();
            String queueName = INSTANCE_QUEUE_PREFIX + appId + "-queues-" + priority;
            try (Jedis jedis = RedisPool.getInstance()) {
                String data = jedis.rpop(queueName);

                if (data != null && !data.equals("nil")) {
                    JobInstance jobInstance = JSON.parseObject(data, JobInstance.class);
                    //sync to client
                    String instanceRunningPoolName = INSTANCE_QUEUE_PREFIX + appId + "-running-pool-" + instanceId;
                    jedis.hset(instanceRunningPoolName, jobInstance.getInstanceId(), JSON.toJSONString(jobInstance));

                    return jobInstance;
                } else {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        log.error("thread has interrupted! ");
                        return null;
                    }
                }
            }
        }
    }

    public void pushIntoLocalRetryQueue(JobInstance instance) {
        localRetryQueue.add(instance);
    }

    public boolean ackJobInstance(JobInstance jobInstance) {
        System.out.println("ack Job : " + jobInstance.getInstanceId());
        //sync to client
        String instanceRunningPoolName = INSTANCE_QUEUE_PREFIX + appId + "-running-pool-" + instanceId;
        try (Jedis jedis = RedisPool.getInstance()) {
            return jedis.hdel(instanceRunningPoolName, jobInstance.getInstanceId()) == 1;
        }
    }

    private JobInstanceChannel(String appId, String instanceId) {
        this.appId = appId;
        this.instanceId = instanceId;


        mqProducer = new DefaultMQProducer("dt");
        mqProducer.setNamesrvAddr("39.108.65.230:9876");
        mqProducer.setSendMsgTimeout(10000);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        consumeTime = System.currentTimeMillis();

        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("dt");
        consumer.setNamesrvAddr("39.108.65.230:9876");
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_TIMESTAMP);
        consumer.setConsumeTimestamp(dateFormat.format(new Date(consumeTime)));
        //set to broadcast mode
        consumer.setMessageModel(MessageModel.BROADCASTING);


        consumer.registerMessageListener((MessageListenerConcurrently) (msgs, context) -> {
            msgs.stream()
                    .filter(msg -> msg.getBornTimestamp() > consumeTime)
                    .forEach(msg -> {
                        JobInstance jobInstance = JSON.parseObject(msg.getBody(), JobInstance.class);
                        commonJobInstanceQueue.add(jobInstance);
                    });
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        try {
            mqProducer.start();
            consumer.subscribe("dt", appId);
            consumer.start();
        } catch (MQClientException e) {
            e.printStackTrace();
        }
    }


    public static void createChannel(String appId, String instanceId) {
        synchronized (JobInstanceChannel.class) {
            jobInstanceChannelHashMap.put(appId, new JobInstanceChannel(appId, instanceId));
        }
    }

    public static JobInstanceChannel getChannel(String appId) {
        return jobInstanceChannelHashMap.get(appId);
    }

}
