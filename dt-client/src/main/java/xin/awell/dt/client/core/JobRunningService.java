package xin.awell.dt.client.core;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import xin.awell.dt.client.context.*;
import xin.awell.dt.client.processer.BaseJobProcessor;
import xin.awell.dt.client.processer.ProcessorContainer;
import xin.awell.dt.client.processer.SimpleJobProcessor;
import xin.awell.dt.core.constant.JobType;
import xin.awell.dt.core.domain.HandleResult;
import xin.awell.dt.core.domain.JobInstance;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author lzp
 * @since 2019/2/2817:28
 */

@Slf4j
public class JobRunningService {
    private JobInstanceChannel channel;

    public JobRunningService(JobInstanceChannel jobInstanceChannel){
        this.channel = jobInstanceChannel;
    }

    private List<Looper> activeLooperList = new ArrayList<>();
    private List<Looper> suspendLooperList = new ArrayList<>();


    public void start(){
        //简单实现
        for(int i = 0; i < 4; i++){
            Looper looper = new Looper();
            looper.start();

            activeLooperList.add(looper);
        }
    }

    public void shutdown(){
        if(activeLooperList != null && activeLooperList.size() != 0){
            activeLooperList.forEach(Looper::shutdown);
        }

        if(suspendLooperList != null && suspendLooperList.size() != 0){
            suspendLooperList.forEach(Looper::shutdown);
        }
    }

    private class Looper extends Thread{
        private static final int INIT = -1;
        private static final int RUNNING = 0;
        private static final int SUSPENDED = 1;
        private static final int SHUTDOWN = 2;
        private static final int TERMINATED = 3;
        private AtomicInteger status;

        private ReentrantLock lock;
        private Condition resumeCondition;

        @Getter
        private Date gmtStopAt;

        public Looper(){
            status = new AtomicInteger(-1);
            lock = new ReentrantLock();
            resumeCondition = lock.newCondition();
        }

        public void shutdown(){
            status.set(SHUTDOWN);
        }

        public void resumeLooper(){
            if(status.get() >= 2){
                throw new RuntimeException("looper 当前状态为： " + status.get() + ",  不可被resume!");
            }

            try {
                lock.lock();
                status.set(RUNNING);
                resumeCondition.signal();
            }finally {
                lock.unlock();
            }
        }

        public void suspendLooper(){
            if(status.get() >= 1){
                throw new RuntimeException("looper 当前状态为： " + status.get() + ",  不可被suspend!");
            }

            status.set(SUSPENDED);
            gmtStopAt = new Date(System.currentTimeMillis());
        }


        @Override
        public synchronized void start() {
            if(status.get() != -1){
                throw new RuntimeException("Looper已经启动！");
            }

            super.start();
            this.status.set(RUNNING);
        }

        @Override
        public void run() {
            while (true){
                int currentStatus = status.get();
                if(currentStatus == SUSPENDED){
                    try {
                        lock.lock();
                        try {
                            if(status.get() == SUSPENDED){
                                resumeCondition.await();
                                status.set(RUNNING);
                                currentStatus = status.get();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }finally {
                        lock.unlock();
                    }
                }

                JobInstance instance = channel.getJobInstance();
                BaseJobContextImpl context = null;
                BaseJobProcessor processor = null;
                Throwable throwable = null;
                try {
                    switch (instance.getJobType()){
                        case simpleJob:
                            context = new SimpleJobContext(instance.getParentId(), instance.getData(), instance.getJobName(), instance.getGmtCreate(), null);
                            processor = ProcessorContainer.getSimpleJobProcessor(instance.getJobProcessor());
                            break;
                        case parallelJob:
                            context = new ParallelJobContextImpl(instance.getParentId(), instance.getData(), instance.getJobName(), instance.getGmtCreate(), null, instance, channel);
                            processor = ProcessorContainer.getParallelJobProcessor(instance.getJobProcessor());
                            break;
                        case commonJob:
                            context = new CommonJobContext(instance.getParentId(), instance.getData(), instance.getJobName(), instance.getGmtCreate(), null);
                            processor = ProcessorContainer.getCommonJobProcessor(instance.getJobProcessor());
                            break;
                        default: break;
                    }

                    handleJob(instance, context, processor);
                }catch (Throwable e){
                    throwable = e;
                }finally {
                    postContextHandle(context, instance, throwable);
                }

                currentStatus = status.get();
                if(currentStatus == SHUTDOWN){
                    status.set(TERMINATED);
                    break;
                }
            }
        }


        private void handleJob(JobInstance instance, BaseJobContextImpl context, BaseJobProcessor processor){
            if(processor == null){
                log.error("配置异常！ 在类路径下查找不到需要的processor! processorFullPathName: {}", instance.getJobProcessor());
            }else{
                try{
                    processor.process(context);
                    if(context.getResult() != null){
                        context.setResult(HandleResult.SUCCESS);
                    }
                }catch (Exception e){
                    log.error("执行任务出错！ ", e);
                    HandleResult result = buildDefaultFailureResult(instance);
                    context.setResult(result);
                }
            }
        }

        private void postContextHandle(BaseJobContextImpl context, JobInstance instance, Throwable throwable){
            if(throwable != null){
                context.setResult(buildDefaultFailureResult(instance));
            }

            HandleResult result = context.getResult();

            if(result != null){
                if(result.isSuccess()){
                    if(context instanceof ParallelJobContextImpl){
                        ((ParallelJobContextImpl) context).uploadSubJobInstances();
                    }
                    channel.ackJobInstance(instance);
                }else{
                    instance.setLastHandleResult(context.getResult());
                    if(result.isRequireRetry()){
                        retryJobInstance(context, instance);
                    }else{
                        channel.ackJobInstance(instance);
                    }
                }
            }
        }

        private HandleResult buildDefaultFailureResult(JobInstance instance){
            HandleResult result = new HandleResult();
            result.setSuccess(false);

            if(instance.getJobType() != JobType.parallelJob){
                result.setRequireRemoteRetry(false);
            }else{
                result.setRequireRemoteRetry(true);
            }

            if(instance.getHasBeenRetried() > 10){
                result.setRequireRetry(false);
                result.setRequireRemoteRetry(false);
            }else {
                result.setRequireRetry(true);
            }

            return result;
        }

        private void retryJobInstance(BaseJobContextImpl context, JobInstance instance){
            if(instance.getHasBeenRetried() > 100){
                channel.ackJobInstance(instance);
                log.info("自动ack一个重试多次也未能完成的任务！ instance: {}", instance);
                return;
            }

            instance.setHasBeenRetried(instance.getHasBeenRetried() + 1);

            if(context.getResult().isRequireRemoteRetry()){
                channel.sendJobInstance(instance);
                channel.ackJobInstance(instance);
            }else{
                channel.pushIntoLocalRetryQueue(instance);
            }
        }
    }
}
