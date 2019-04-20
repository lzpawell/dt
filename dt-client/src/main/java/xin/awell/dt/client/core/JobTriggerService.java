package xin.awell.dt.client.core;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import xin.awell.dt.client.constant.InstanceStatus;
import xin.awell.dt.client.core.ZKService.StatusChangedListener;
import xin.awell.dt.client.core.ZKService.ZKService;
import xin.awell.dt.client.utils.JobConfigValidator;
import xin.awell.dt.core.domain.JobConfigDO;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lzp
 * @since 2019/2/2422:18
 */
@Slf4j
public class JobTriggerService {
    private HashMap<String, JobConfigDO> jobConfigDataHashMap = new HashMap<>();
    private Scheduler scheduler;
    private ZKService zkService;
    private ConfigDataSynchronizer configDataSynchronizer;
    private ConfigDataSynchronizer.OnConfigDataUpdatedListener configDataUpdatedListener;
    private StatusChangedListener statusChangedListener;

    public JobTriggerService(ZKService zkService, ConfigDataSynchronizer configDataSynchronizer) throws SchedulerException {
        this.zkService = zkService;
        this.configDataSynchronizer = configDataSynchronizer;
        scheduler = StdSchedulerFactory.getDefaultScheduler();

        configDataUpdatedListener = newJobConfigDOList -> {
            List<JobConfigDO> activeJobConfigDOList = validateJobConfig(activeJobConfigFilter(newJobConfigDOList));
            //create or update
            activeJobConfigDOList.forEach(this::resetJob);

            Collection<JobConfigDO> deletedList = jobConfigDataHashMap.values()
                    .stream()
                    .filter(jobConfigDO -> {
                        boolean found = false;
                        for(JobConfigDO configDO : activeJobConfigDOList){
                            if(Objects.equals(configDO.getJobId(), jobConfigDO.getJobId())){
                                found = true;
                                break;
                            }
                        }

                        return !found;
                    })
                    .collect(Collectors.toList());

            //delete
            deletedList.stream().map(JobConfigDO::getJobId).forEach(jobConfigDataHashMap::remove);
            deletedList.forEach(this::removeJob);
        };

        statusChangedListener = instanceStatus -> {
            System.out.println(instanceStatus);
            if(instanceStatus == InstanceStatus.LEADER){
                try {
                    resume();
                } catch (SchedulerException e) {
                    log.error("trigger Service resume failed! msg : {}", e.getMessage());
                }
            }else {
                try {
                    stop();
                } catch (SchedulerException e) {
                    log.error("trigger Service stop failed! msg : {}", e.getMessage());
                }
            }
        };
    }

    @Getter
    private boolean running;

    public void start() throws SchedulerException {
        zkService.addListener(statusChangedListener);
        scheduler.start();

        if(zkService.getCurrentInstanceStatus() == InstanceStatus.LEADER){
            statusChangedListener.onStatusChanged(InstanceStatus.LEADER);
        }
    }


    private void removeJob(JobConfigDO configDO) {
        if(configDO != null && configDO.getJobId() != null){
            int retry = 3;
            while (retry-- > 0){
                try {
                    boolean success = scheduler.deleteJob(JobKey.jobKey(configDO.getJobId(), configDO.getAppId()));
                    if(success){
                        retry = 0;
                    }
                } catch (SchedulerException e) {
                    log.error("delete job failed! msg{}", e.getMessage());
                }
            }
        }
    }


    private static Trigger createTrigger(JobConfigDO jobConfigDO){
        return TriggerBuilder.newTrigger()
                .withIdentity(jobConfigDO.getJobId(), jobConfigDO.getAppId())
                .startNow()
                .withSchedule(CronScheduleBuilder.cronSchedule(jobConfigDO.getCronExp()))
                .build();
    }

    private static JobDetail createJobDetails(JobConfigDO configDO){
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("config", configDO);
        return JobBuilder.newJob(DtJobProcessor.class)
                .setJobData(jobDataMap).withIdentity(new JobKey(configDO.getJobId(), configDO.getAppId()))
                .build();
    }

    private void resetJob(JobConfigDO jobConfigDO)  {
        JobConfigDO oldJobConfigDO = jobConfigDataHashMap.get(jobConfigDO.getJobId());
        if(!Objects.equals(oldJobConfigDO, jobConfigDO)){
            removeJob(oldJobConfigDO);

            jobConfigDataHashMap.put(jobConfigDO.getJobId(), jobConfigDO);

            try {
                scheduler.scheduleJob(createJobDetails(jobConfigDO), createTrigger(jobConfigDO));
            } catch (SchedulerException e) {
                log.error("scheduleJob failed! msg : {}", e.getMessage());
            }
        }
    }

    private void stop() throws SchedulerException {
        this.running = false;
        scheduler.deleteJobs(jobConfigDataHashMap.values()
                .stream().map(configDO -> JobKey.jobKey(configDO.getJobId(), configDO.getAppId())).collect(Collectors.toList()));
    }

    private void resume() throws SchedulerException {
        if(this.running){
            return;
        }

        this.running = true;
        jobConfigDataHashMap = new HashMap<>();

        configDataSynchronizer.unsubscribeDataUpdate(configDataUpdatedListener);
        configDataSynchronizer.subscribeDataUpdate(configDataUpdatedListener);

        configDataSynchronizer.getLastJobConfigDOList().forEach(this::resetJob);
    }

    public void shutdown() throws SchedulerException {
        configDataSynchronizer.unsubscribeDataUpdate(configDataUpdatedListener);
        zkService.removeListener(statusChangedListener);
        scheduler.shutdown();
        this.running = false;
    }


    private List<JobConfigDO> activeJobConfigFilter(@NonNull List<JobConfigDO> configDOList){
        return configDOList.stream()
                .filter(JobConfigDO::getEnable)
                .collect(Collectors.toList());
    }

    private List<JobConfigDO> validateJobConfig(@NonNull List<JobConfigDO> configDOList){
        return configDOList.stream()
                .filter(JobConfigValidator::validateJobConfig)
                .collect(Collectors.toList());
    }

}
