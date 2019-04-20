package xin.awell.dt.client.core;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import xin.awell.dt.client.utils.RedisPool;
import xin.awell.dt.core.constant.JobPriority;
import xin.awell.dt.core.constant.JobType;
import xin.awell.dt.core.domain.JobInstance;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;

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
    private Queue<JobInstance> localRetryQueue = new ArrayDeque<>();

    public boolean sendJobInstance(JobInstance jobInstance){
        if(jobInstance.getJobType() == JobType.commonJob){

        }else{
            String queueName = INSTANCE_QUEUE_PREFIX + appId + "-queues-" + jobInstance.getPriority();
            try(Jedis jedis = RedisPool.getInstance()){
                Long result = jedis.lpush(queueName, JSON.toJSONString(jobInstance));
                System.out.println("send! " + result);
            }
            return true;
        }
        //简单实现， 先投递到本机
        return localRetryQueue.offer(jobInstance);
    }

    public boolean sendJobInstanceList(List<JobInstance> instanceList){
        instanceList.forEach(this::sendJobInstance);
        return true;
    }

    private JobPriority getTargetPriority(){
        double random = Math.random();

        //p1 p2 p3 p4 :: 4:3:2:1
        if(random < 0.1){
            return JobPriority.p4;
        }else if(random < 0.3){
            return JobPriority.p3;
        }else if(random < 0.6){
            return JobPriority.p2;
        }else {
            return JobPriority.p1;
        }
    }

    public JobInstance getJobInstance(){
        if(localRetryQueue.size() != 0){
            synchronized (localRetryQueue){
                JobInstance jobInstance = localRetryQueue.poll();
                if(jobInstance != null){
                    return jobInstance;
                }
            }
        }

        /*
        可能在同步之前就localQueue就空了， 那么取分布式队列的
        循环直到真正取出了一个任务后， 取不到任务， 表示目标队列可能是空的
         */
        while (true){
            JobPriority priority = getTargetPriority();
            String queueName = INSTANCE_QUEUE_PREFIX + appId + "-queues-" + priority;
            try(Jedis jedis = RedisPool.getInstance()){
                String data = jedis.rpop(queueName);

                if(data != null && !data.equals("nil")){
                    JobInstance jobInstance = JSON.parseObject(data, JobInstance.class);
                    //sync to client
                    String instanceRunningPoolName = INSTANCE_QUEUE_PREFIX + appId + "-running-pool-" + instanceId;
                    jedis.hset(instanceRunningPoolName, jobInstance.getInstanceId(), JSON.toJSONString(jobInstance));

                    return jobInstance;
                }else{
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

    public void pushIntoLocalRetryQueue(JobInstance instance){
        localRetryQueue.add(instance);
    }

    public boolean ackJobInstance(JobInstance jobInstance){
        System.out.println("ack Job : " + jobInstance.getInstanceId());
        //sync to client
        String instanceRunningPoolName = INSTANCE_QUEUE_PREFIX + appId + "-running-pool-" + instanceId;
        try(Jedis jedis = RedisPool.getInstance()){
            return jedis.hdel(instanceRunningPoolName, jobInstance.getInstanceId()) == 1;
        }
    }

    private JobInstanceChannel(String appId, String instanceId){
        this.appId = appId;
        this.instanceId = instanceId;
    }


    public static void createChannel(String appId, String instanceId){
        synchronized (JobInstanceChannel.class){
            jobInstanceChannelHashMap.put(appId, new JobInstanceChannel(appId, instanceId));
        }
    }

    public static JobInstanceChannel getChannel(String appId){
        return jobInstanceChannelHashMap.get(appId);
    }

}
