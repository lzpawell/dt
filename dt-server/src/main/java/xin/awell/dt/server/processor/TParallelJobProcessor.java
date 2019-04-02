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
        System.out.println("处理并行任务： " + JSON.toJSONString(taskContext));

        if(taskContext.getJobName() == JobInstance.DEFAULT_JOB_NAME){
            List<SubJobInstance> subJobInstances = new ArrayList<>();
            for(int i = 0; i < 10; i++){
                SubJobInstance instance = new SubJobInstance();
                instance.setData("" + i);
                instance.setSubJobName("subName");
                subJobInstances.add(instance);
            }

            taskContext.distributeSubJob(subJobInstances);
        }


        taskContext.setResult(HandleResult.SUCCESS);
    }
}
