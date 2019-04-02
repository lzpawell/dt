package xin.awell.dt.client.core;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.quartz.SchedulerException;
import xin.awell.dt.client.core.ZKService.ZKService;
import xin.awell.dt.client.core.ZKService.ZKServiceImpl;

import java.io.IOException;

/**
 * @author lzp
 * @since 2018/11/2715:54
 */
@Data
@Accessors(chain = true)
@Slf4j
public class DtClient {
    private String appId;
    private String zkAddressStr;
    private ZKService zkService;
    private ConfigDataSynchronizer configDataSynchronizer;
    private JobTriggerService jobTriggerService;
    private JobRunningService jobRunningService;
    private JobInstanceChannel channel;

    public DtClient(@NonNull String zkAddressStr, @NonNull String appId){
        this.appId = appId;
        this.zkAddressStr = zkAddressStr;
    }

    public void start() throws Exception {
        zkService = new ZKServiceImpl(zkAddressStr, appId);
        zkService.start();

        configDataSynchronizer = new ConfigDataSynchronizer(zkService.getCuratorInstance(), appId);
        configDataSynchronizer.init();

        jobTriggerService = new JobTriggerService(zkService, configDataSynchronizer);
        jobTriggerService.start();

        channel = JobInstanceChannel.getChannel(appId);

        jobRunningService = new JobRunningService(channel);
        jobRunningService.start();
    }

    public void shutdown() throws SchedulerException, IOException {
        if(jobRunningService != null){
            jobRunningService.shutdown();
        }

        if(jobTriggerService != null){
            jobTriggerService.shutdown();
        }

        if(configDataSynchronizer != null){
            configDataSynchronizer.close();
        }

        if(zkService != null){
            zkService.shutdown();
        }
    }

}
