package xin.awell.dt.client.core.ZKService;

import org.apache.curator.framework.CuratorFramework;
import xin.awell.dt.client.constant.InstanceStatus;

/**
 * @author lzp
 * @since 2019/2/2314:30
 */
public interface ZKService {

    void start() throws Exception;

    void shutdown();

    InstanceStatus getCurrentInstanceStatus() throws ZKException;

    void addListener(StatusChangedListener listener);

    void removeListener(StatusChangedListener listener);

    CuratorFramework getCuratorInstance();
}
