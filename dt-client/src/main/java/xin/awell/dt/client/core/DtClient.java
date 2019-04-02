package xin.awell.dt.client.core;

import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.*;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.io.IOException;

/**
 * @author lzp
 * @since 2018/11/2715:54
 */
@Data
@Accessors(chain = true)
@Slf4j
public class DtClient {
    private String clientClusterId;
    private boolean leader = false;
    private String ZK_ADDRESS = "39.108.65.230:2181";
    private CuratorFramework zkClient;
    private LeaderLatch leaderLatch;

    public DtClient(@NonNull String ZK_ADDRESS, @NonNull String clientClusterId){
        this.ZK_ADDRESS = ZK_ADDRESS;
        this.clientClusterId = clientClusterId;
    }

    public void start() throws InterruptedException {
        zkClient = CuratorFrameworkFactory.newClient(
                ZK_ADDRESS,
                new ExponentialBackoffRetry(1000, 3)
        );
        zkClient.start();

        String path = "/" + clientClusterId;


        leaderLatch = new LeaderLatch(zkClient, path, zkClient.toString());

        try {
            leaderLatch.start();



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown(){

    }




    public void close() {
        if(leaderLatch != null){
            try {
                leaderLatch.close(LeaderLatch.CloseMode.NOTIFY_LEADER);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(zkClient != null){
            zkClient.close();
        }
    }
}
