package xin.awell.dt.core.constant;

/**
 * @author lzp
 * @since 2019/2/191:28
 */
public enum JobType {
    /**
     * 简单任务
     */
    simpleJob,

    /**
     * 集群任务,各个节点都运行一次
     */
    commonJob,


    /**
     * 分布式并行任务
     */
    parallelJob

}
