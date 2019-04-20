package xin.awell.dt.client.utils;

import org.quartz.CronExpression;
import xin.awell.dt.client.processer.ProcessorContainer;
import xin.awell.dt.core.constant.TriggerMode;
import xin.awell.dt.core.domain.JobConfigDO;

/**
 * @author lzp
 * @since 2019/4/314:18
 */
public class JobConfigValidator {
    public static boolean validateJobConfig(JobConfigDO configDO){
        if(configDO.getTriggerMode() == null){
            return false;
        }

        if(configDO.getTriggerMode() == TriggerMode.cronExp){
            return validateCronExp(configDO) && validateJobTypeAndProcessor(configDO);
        }else {
            return validateJobTypeAndProcessor(configDO);
        }
    }

    private static boolean validateJobTypeAndProcessor(JobConfigDO jobConfigDO){
        if(jobConfigDO.getJobType() == null || jobConfigDO.getJobProcessor() == null){
            return false;
        }

        switch (jobConfigDO.getJobType()){
            case parallelJob:
                if(ProcessorContainer.getParallelJobProcessor(jobConfigDO.getJobProcessor()) == null){
                    return false;
                }
                break;
            case simpleJob:
                if(ProcessorContainer.getSimpleJobProcessor(jobConfigDO.getJobProcessor()) == null){
                    return true;
                }
                break;
            case commonJob:
                if(ProcessorContainer.getCommonJobProcessor(jobConfigDO.getJobProcessor()) == null){
                    return false;
                }
                break;
            default:return false;
        }

        return true;
    }

    private static boolean validateCronExp(JobConfigDO configDO){
        if(configDO.getCronExp() == null || "".equals(configDO.getCronExp())){
            return false;
        }

        return CronExpression.isValidExpression(configDO.getCronExp());
    }
}
