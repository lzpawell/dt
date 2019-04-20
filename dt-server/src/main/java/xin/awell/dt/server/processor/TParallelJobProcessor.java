package xin.awell.dt.server.processor;

import com.alibaba.fastjson.JSON;
import xin.awell.dt.client.context.ParallelJobContext;
import xin.awell.dt.client.domain.SubJobInstance;
import xin.awell.dt.client.processer.ParallelJobProcessor;
import xin.awell.dt.core.domain.HandleResult;
import xin.awell.dt.core.domain.JobInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lzp
 * @since 2019/4/12:15
 */
public class TParallelJobProcessor implements ParallelJobProcessor {
    @Override
    public void process(ParallelJobContext taskContext) {

        if(JobInstance.DEFAULT_JOB_NAME.equals(taskContext.getJobName())){
            System.out.println("处理并行任务： " + JSON.toJSONString(taskContext));
            List<SubJobInstance> subJobInstances = new ArrayList<>();
            for(int i = 0; i < 10; i++){
                SubJobInstance instance = new SubJobInstance();
                instance.setData("" + i);
                instance.setSubJobName("subName");
                subJobInstances.add(instance);
            }
            System.out.println("分发子任务！ ");
            taskContext.distributeSubJob(subJobInstances);
        }else{
            System.out.println("正在处理子任务： " + "name: " + taskContext.getJobName() + "   data:  " + taskContext.getData());
        }


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        taskContext.setResult(HandleResult.SUCCESS);
    }
}
