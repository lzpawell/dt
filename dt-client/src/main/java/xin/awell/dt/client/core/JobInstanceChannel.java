package xin.awell.dt.client.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
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

    private Queue<JobInstance> localRetryQueue = new ArrayDeque<>();

    public boolean sendJobInstance(JobInstance jobInstance){
        System.out.println(jobInstance.toString());

        //简单实现， 先投递到本机
        return localRetryQueue.offer(jobInstance);
    }

    public boolean sendJobInstanceList(List<JobInstance> instanceList){
        instanceList.forEach(this::sendJobInstance);
        return true;
    }

    public JobInstance getJobInstance(){

        synchronized (localRetryQueue){
            while (true){
                JobInstance jobInstance = localRetryQueue.poll();
                if(jobInstance == null){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else{
                    return jobInstance;
                }
            }
        }
    }

    public void pushIntoLocalRetryQueue(JobInstance instance){
        localRetryQueue.add(instance);
    }

    public boolean ackJobInstance(JobInstance jobInstance){
        return true;
    }

    private JobInstanceChannel(){

    }


    public static JobInstanceChannel getChannel(String appId){
        JobInstanceChannel channel = jobInstanceChannelHashMap.get(appId);
        if(channel == null){
            synchronized (JobInstanceChannel.class){
                if(jobInstanceChannelHashMap.get(appId) == null) {
                    channel = new JobInstanceChannel();
                    jobInstanceChannelHashMap.put(appId, channel);
                }
            }
        }

        return channel;
    }

    @Getter
    private static JobInstanceChannel instance;
}
