package xin.awell.dt.client.core;

import com.alibaba.fastjson.JSON;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import redis.clients.jedis.Jedis;
import xin.awell.dt.client.utils.RedisPool;
import xin.awell.dt.core.domain.JobInstance;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lzp
 * @since 2019/4/210:23
 */
public class JobInstanceRecovery {
    private CuratorFramework client;
    private String appId;
    private static final String DT_INSTANCE_ROOT_PATH = "/instance";
    private static String INSTANCE_QUEUE_PREFIX = "dt-job-instance-";
    public JobInstanceRecovery(CuratorFramework client, String appId){
        this.client = client;
        this.appId = appId;
    }

    public void start() throws Exception {
        PathChildrenCache cache = new PathChildrenCache(client, DT_INSTANCE_ROOT_PATH, true);
        cache.start();
        cache.getListenable().addListener((client, event) -> {
            if(event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED){
                String instanceId = event.getData().getPath();
                instanceId = instanceId.substring(instanceId.lastIndexOf('/') + 1);

                System.out.println("失联： " + instanceId);
                repushJobInstances(instanceId);
            }
        });
    }


    private void repushJobInstances(String instanceId){
        String instanceRunningPoolName = INSTANCE_QUEUE_PREFIX + appId + "-running-pool-" + instanceId;
        try(Jedis jedis = RedisPool.getInstance()){
            Map<String, String> dataMap = jedis.hgetAll(instanceRunningPoolName);
            JobInstanceChannel.getChannel(appId).sendJobInstanceList(
                    dataMap.values().stream()
                    .map(rawJobInstance -> JSON.parseObject(rawJobInstance, JobInstance.class))
                    .collect(Collectors.toList())
            );
        }
    }
}
