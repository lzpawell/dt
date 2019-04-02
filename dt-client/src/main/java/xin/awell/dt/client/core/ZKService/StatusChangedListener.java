package xin.awell.dt.client.core.ZKService;

import xin.awell.dt.client.constant.InstanceStatus;

/**
 * @author lzp
 * @since 2019/2/2314:34
 */
public interface StatusChangedListener {
    /**
     * ZKService会在服务状态改变时通过listener回调onStatusChanged
     * @param instanceStatus
     */
    void onStatusChanged(InstanceStatus instanceStatus);
}
