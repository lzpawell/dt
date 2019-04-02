package xin.awell.dt.client.core.ZKService;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.leader.Participant;
import org.apache.curator.retry.ExponentialBackoffRetry;
import xin.awell.dt.client.constant.InstanceStatus;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author lzp
 * @since 2019/2/2314:31
 */
@Slf4j
public class ZKServiceImpl implements ZKService{

    private String zkConnectString;
    private CuratorFramework client;
    private LeaderLatch leaderLatch;
    private String appId;
    private String instanceId;
    private List<StatusChangedListener> statusChangedListenerList = new ArrayList<>();

    private boolean isConnected() {
        return connected;
    }

    private void setConnected(boolean connected) {
        this.connected = connected;
    }

    private boolean connected;

    private boolean isLeader() {
        return leader;
    }

    private void setLeader(boolean leader) {
        this.leader = leader;
    }

    private boolean leader;
    private InstanceStatus currentInstanceStatus;
    public ZKServiceImpl(@NonNull String zkConnectString, @NonNull String appId){
        this.zkConnectString = zkConnectString;
        this.appId = appId;
        connected = false;
        leader = false;
        currentInstanceStatus = InstanceStatus.UNKNOWN;
        this.instanceId = UUID.randomUUID().toString();
    }

    @Override
    public void start() throws Exception {
        if(client != null && client.getState() == CuratorFrameworkState.STARTED){
            return;
        }

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        client = CuratorFrameworkFactory.builder()
                .connectString(zkConnectString)
                .retryPolicy(retryPolicy)
                .sessionTimeoutMs(60000)
                .connectionTimeoutMs(3000)
                .namespace("dt")
                .build();

        client.getConnectionStateListenable().addListener((curatorFramework, connectionState) -> {
            switch (connectionState){
                case CONNECTED:
                    connected = true;
                    break;
                case RECONNECTED:
                    connected = true;
                    checkStatus();
                    break;
                case SUSPENDED:
                case LOST:
                    connected = false;
                    freshStatus();
                    break;

                    default:break;
            }
        });
        client.start();


        String electionPath = "/election/" + appId;
        leaderLatch = new LeaderLatch(client, electionPath, instanceId);
        leaderLatch.addListener(new LeaderLatchListener() {
            @Override
            public void isLeader() {
                log.info("Currently run as leader");
                setLeader(true);
                freshStatus();
            }

            //挂起后主节点也会调用一次这个函数
            @Override
            public void notLeader() {
                log.info("no leader has been invoked!");
                setLeader(false);
                freshStatus();
            }
        });
        leaderLatch.start();

        checkStatus();
    }


    private void freshStatus(){
        InstanceStatus lastInstanceStatus = currentInstanceStatus;
        if(!isConnected()){
            if(lastInstanceStatus != InstanceStatus.SUSPENDED){
                currentInstanceStatus = InstanceStatus.SUSPENDED;
                statusChangedListenerList.forEach(listener -> listener.onStatusChanged(currentInstanceStatus));
            }
        }else{
            if(isLeader() && lastInstanceStatus != InstanceStatus.LEADER){
                currentInstanceStatus = InstanceStatus.LEADER;
                statusChangedListenerList.forEach(listener -> listener.onStatusChanged(currentInstanceStatus));
            }else if(!isLeader() && lastInstanceStatus != InstanceStatus.WORKER){
                currentInstanceStatus = InstanceStatus.WORKER;
                statusChangedListenerList.forEach(listener -> listener.onStatusChanged(currentInstanceStatus));
            }
        }
    }

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(4, 4,60, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

    @SuppressWarnings("all")
    private void checkStatus(){
        executor.execute(()->{
            while (currentInstanceStatus == InstanceStatus.UNKNOWN || currentInstanceStatus == InstanceStatus.SUSPENDED){
                if(leaderLatch != null){
                    try {
                        Participant leader = leaderLatch.getLeader();
                        //查代码可知， leader.getId equals "", 当getLeader失败时
                        if(Objects.equals(leader.getId(), "")){
                            Thread.sleep(5000);
                            continue;
                        }

                        boolean currentLeadership = Objects.equals(leader.getId(), instanceId);

                        if(currentInstanceStatus == InstanceStatus.UNKNOWN && currentLeadership == false){
                            setLeader(false);
                            freshStatus();
                            return;
                        }

                        if(currentInstanceStatus == InstanceStatus.SUSPENDED){
                            setLeader(currentLeadership);
                            freshStatus();
                        }
                    } catch (Exception e) {
                        log.error("get leader error! ", e);
                    }
                }
            }
        });
    }

    @Override
    public void shutdown() {
        if(leaderLatch != null){
            try {
                leaderLatch.close();
            } catch (IOException e) {
                log.error("leaderLeatch close error! ", e);
            }
        }

        if(client != null && client.getState() != CuratorFrameworkState.STOPPED){
            client.close();
        }

        if(executor != null){
            executor.shutdown();
        }
    }


    @Override
    public InstanceStatus getCurrentInstanceStatus() {
        return currentInstanceStatus;
    }

    @Override
    public void addListener(StatusChangedListener listener) {
        //PathChildrenCache
        //TreeCache jobConfigCache = new TreeCache(client, "/job_config/" + appId);
        if(!statusChangedListenerList.contains(listener)){
            statusChangedListenerList.add(listener);
        }
    }

    @Override
    public void removeListener(StatusChangedListener listener) {
        statusChangedListenerList.remove(listener);
    }

    @Override
    public CuratorFramework getCuratorInstance() {
        return client;
    }
}
