package xin.awell.dt.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import xin.awell.dt.core.constant.TriggerMode;
import xin.awell.dt.core.domain.DataResult;
import xin.awell.dt.core.domain.JobConfigDO;
import xin.awell.dt.server.config.ZKConfig;
import xin.awell.dt.server.service.ZKService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lzp
 * @since 2019/2/2323:10
 */
@Service
@Slf4j
public class ZKServiceImpl implements ZKService , InitializingBean, DisposableBean {

    private CuratorFramework client;

    @Getter
    private boolean connected;

    @Autowired
    private ZKConfig zkConfig;

    private static final String ROOT_CONFIG_PATH = "/job_config";

    @Override
    public List<JobConfigDO> listConfigures(String appId)  {
        if(!isConnected()){
            log.error("lost zk connect!");
            return null;
        }

        String appConfigPath = ROOT_CONFIG_PATH + "/" +  appId;

        try {
            List<JobConfigDO> configDOList = new ArrayList<>();
            for(String subPath : client.getChildren().forPath(appConfigPath)){
                byte[] bytes = client.getData().forPath(appConfigPath + "/" + subPath);
                JobConfigDO configDO = JSON.parseObject(bytes, JobConfigDO.class);
                configDOList.add(configDO);
            }

            return configDOList;
        } catch (Exception e) {
            log.error("list config error! ", e);
        }

        return null;
    }

    @Override
    public boolean setJob(JobConfigDO data) {
        if(!preCheckConfig(data)){
            log.error("config data illeal!");
            return false;
        }

        if(!isConnected()){
            log.error("lost zk connect!");
            return false;
        }
        String jobPath = ROOT_CONFIG_PATH + "/" + data.getAppId() + "/" + data.getJobId();
        try {
            Stat checkExistsSstat = client.checkExists().forPath(jobPath);
            if(checkExistsSstat == null){
                client.create().creatingParentContainersIfNeeded().forPath(jobPath, JSON.toJSONBytes(data));
            }else{
                client.setData().forPath(jobPath, JSON.toJSONBytes(data));
            }
            return true;
        } catch (Exception e) {
            log.error("zkService putData error! ", e);
            return false;
        }
    }

    /**
     * 预检查配置完整性和合法性
     * @param data
     * @return
     */
    private boolean preCheckConfig(JobConfigDO data) {
        if(data.getAppId() == null
                || data.getEnable() == null
                || data.getJobDesc() == null
                || data.getJobId() == null
                || data.getJobProcessor() == null
                || data.getJobType() == null
                || data.getTriggerMode() == null){
            return false;
        }

        if(data.getTriggerMode() == TriggerMode.cronExp && data.getCronExp() == null){
            return false;
        }

        return true;
    }

    @Override
    public boolean deleteJob(String appId, String jobId) {
        if(!isConnected()){
            log.error("lost zk connect!");
            return false;
        }
        String appJobsConfigPath = ROOT_CONFIG_PATH + "/" + appId + "/" + jobId;

        try {
            client.delete().guaranteed().forPath(appJobsConfigPath);
            return true;
        } catch (Exception e) {
            log.error("zkService deleteData error! ", e);
            return false;
        }
    }

    @Override
    public boolean deleteApp(String appId) {
        if(!isConnected()){
            log.error("lost zk connect!");
            return false;
        }
        String appPath = ROOT_CONFIG_PATH + "/" + appId;

        try {
            client.delete().guaranteed().deletingChildrenIfNeeded().forPath(appPath);
            return true;
        } catch (Exception e) {
            log.error("zkService deleteData error! ", e);
            return false;
        }
    }

    @Override
    public boolean createApp(String appId) {
        if(!isConnected()){
            log.error("lost zk connect!");
            return false;
        }
        String appJobsConfigPath = ROOT_CONFIG_PATH + "/" + appId;

        try {
            client.create().creatingParentContainersIfNeeded().forPath(appJobsConfigPath);
            return true;
        } catch (Exception e) {
            log.error("add node failed! msg : {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void destroy() throws Exception {
        log.info("on ZKService shutdown!");
        if(client != null && client.getState() != CuratorFrameworkState.STOPPED){
            client.close();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(zkConfig.getConnectionString())
                .retryPolicy(retryPolicy)
                .sessionTimeoutMs(60000)
                .connectionTimeoutMs(3000)
                .namespace("dt")
                .build();

        client.getConnectionStateListenable().addListener((curatorFramework, connectionState) -> {
            switch (connectionState){
                case CONNECTED:
                case RECONNECTED:
                    connected = true;
                    break;

                case SUSPENDED:
                case LOST:
                    connected = false;
                    break;

                default:break;
            }
        });
        client.start();
    }

}
