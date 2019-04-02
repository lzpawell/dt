package xin.awell.dt.client.constant;

/**
 * @author lzp
 * @since 2019/2/2314:35
 */
public enum InstanceStatus {
    /**
     * 主节点
     */
    LEADER,

    /**
     * 工作节点
     */
    WORKER,

    /**
     * 与zookeeper断连，当前处于挂起状态
     */
    SUSPENDED,

    /**
     * 初始化过程， 未知状态
     */
    UNKNOWN
}
