package xin.awell.dt.core.domain;

import com.sun.nio.sctp.HandlerResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import xin.awell.dt.core.constant.JobType;
import xin.awell.dt.core.constant.TriggerMode;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * @author lzp
 * @since 2019/2/2621:40
 */
@Data
@Accessors(chain = true)
@ToString
public class JobInstance {
    private String jobId;
    private String appId;
    private String parentId;
    private String instanceId;
    private String jobProcessor;
    private String runtimeParas;
    private String jobName;
    private Serializable data;
    private JobType jobType;
    private Date gmtCreate;
    private HandleResult lastHandleResult;
    private int  hasBeenRetried;

    public static String DEFAULT_JOB_NAME = "default";

    public static JobInstance jobConfigToInstance(JobConfigDO configDO){
        JobInstance instance = new JobInstance();
        instance.setAppId(configDO.getAppId())
                .setData(null)
                .setGmtCreate(new Date())
                .setJobId(configDO.getJobId())
                .setJobProcessor(configDO.getJobProcessor())
                .setRuntimeParas(configDO.getRuntimeParas())
                .setJobType(configDO.getJobType())
                .setInstanceId(UUID.randomUUID().toString())
                .setHasBeenRetried(0).setJobName(DEFAULT_JOB_NAME);

        return instance;
    }

    public static JobInstance createSubJobInstance(JobInstance instance, Serializable data, String subJobName){
        JobInstance subInstance =  new JobInstance();
        subInstance.setAppId(instance.getAppId())
                .setInstanceId(UUID.randomUUID().toString())
                .setData(data)
                .setGmtCreate(new Date())
                .setJobId(instance.getJobId())
                .setJobProcessor(instance.getJobProcessor())
                .setRuntimeParas(instance.getRuntimeParas())
                .setJobType(instance.getJobType())
                .setHasBeenRetried(0)
                .setJobName(subJobName)
                .setData(data);

        subInstance.setParentId(instance.getInstanceId());
        return subInstance;
    }
}
