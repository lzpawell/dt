package xin.awell.dt.client.core;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import xin.awell.dt.core.constant.JobType;
import xin.awell.dt.core.domain.JobConfigDO;
import xin.awell.dt.core.domain.JobInstance;

/**
 * @author lzp
 * @since 2019/2/2817:00
 * 用于生成quartzJob
 * 如果是分布式任务， 推入分布式任务队列
 * 如果是本地任务， 推入本地任务队列
 */
public class DtJobProcessor implements Job{

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        JobConfigDO configDO = (JobConfigDO) dataMap.get("config");

        //生成job运行时实例
        JobInstance jobInstance = JobInstance.jobConfigToInstance(configDO);
        try{
            JobInstanceChannel.getChannel(configDO.getAppId()).sendJobInstance(jobInstance);
        }catch (Exception e){
            System.out.println("push job instance failed!, jobInstance: {"+ jobInstance.toString() +"}, errMsg: {"+ e.getMessage() + "}");
        }
    }
}