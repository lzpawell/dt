package xin.awell.dt.client.core;

import com.alibaba.fastjson.JSON;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import xin.awell.dt.core.domain.JobConfigDO;

import java.io.IOException;
import java.util.*;

/**
 * @author lzp
 * @since 2019/2/2422:25
 * 配置在zookeeper中的路径： /job_config/appId/jobId  --> jobConfigDO
 */
@Slf4j
public class ConfigDataSynchronizer {
    private Map<String, JobConfigDO> id2ConfigDataMap = new HashMap<>();

    @Getter
    private List<JobConfigDO> lastJobConfigDOList = new ArrayList<>();
    private List<OnConfigDataUpdatedListener> listeners = new ArrayList<>();
    private String appId;
    private static final String DT_CONFIG_ROOT_PATH = "/job_config";
    private CuratorFramework client;
    private PathChildrenCache cache;

    @Getter
    @Setter
    private boolean running;

    public void subscribeDataUpdate(OnConfigDataUpdatedListener listener){
        if(listeners.stream()
                .noneMatch(theListener -> theListener == listener)){
            listeners.add(listener);
        }
    }

    public void unsubscribeDataUpdate(OnConfigDataUpdatedListener listener){
        listeners.remove(listener);
    }

    public ConfigDataSynchronizer(CuratorFramework client, String appId){
        this.appId = appId;
        this.client = client;
    }

    /**
     *
     * @throws Exception
     */
    public void init() throws Exception {
        cache = new PathChildrenCache(client, DT_CONFIG_ROOT_PATH + "/" + appId, true);
        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        cache.getListenable().addListener((client, event) -> {
            JobConfigDO jobConfigDO = null;
            switch (event.getType()){
                case INITIALIZED:
                    /*event.getInitialData().forEach(childData -> {
                        JobConfigDO configData = JSON.parseObject(childData.getData(), JobConfigDO.class);
                        id2ConfigDataMap.put(configData.getJobId(), configData);
                    });*/
                    break;
                case CHILD_ADDED:
                    jobConfigDO = JSON.parseObject(event.getData().getData(), JobConfigDO.class);
                    id2ConfigDataMap.put(jobConfigDO.getJobId(), jobConfigDO);
                    break;
                case CHILD_UPDATED:
                    jobConfigDO = JSON.parseObject(event.getData().getData(), JobConfigDO.class);
                    id2ConfigDataMap.put(jobConfigDO.getJobId(), jobConfigDO);
                    break;
                case CHILD_REMOVED:
                    String id = event.getData().getPath();
                    id = id.substring(id.lastIndexOf('/') + 1);
                    id2ConfigDataMap.remove(id);
                    default:break;
            }

            List<JobConfigDO> jobConfigDOList = new ArrayList<>(id2ConfigDataMap.values());


            if(lastJobConfigDOList == null){
                lastJobConfigDOList = new ArrayList<>();
            }

            if(!lastJobConfigDOList.containsAll(jobConfigDOList) || !jobConfigDOList.containsAll(lastJobConfigDOList)){
                listeners.forEach(listener -> {
                    try{
                        listener.configDataUpdated(jobConfigDOList);
                    }catch (Exception e){
                        log.error("configDataSynchronizer notify listener error! when invoke listener#configDataUpdated!", e);
                    }
                });

                lastJobConfigDOList = jobConfigDOList;
            }
        });
    }

    public void close() throws IOException {
        if(cache != null){
            cache.close();
        }

        if(client != null){
            client.close();
        }
    }

    public interface OnConfigDataUpdatedListener{
        /**
         * 第一次configDataMap有更新时回调这个方法。
         * @param newJobConfigDOList
         */
        void configDataUpdated(List<JobConfigDO> newJobConfigDOList);
    }
}
