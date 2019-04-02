package xin.awell.dt.client.context;

import xin.awell.dt.client.context.ParallelJobContext;
import xin.awell.dt.client.core.JobInstanceChannel;
import xin.awell.dt.client.domain.SubJobInstance;
import xin.awell.dt.core.domain.JobInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lzp
 * @since 2019/4/218:37
 */
public class ParallelJobContextImpl extends ParallelJobContext {

    private List<List<SubJobInstance>> subJobInstanceLists;
    private JobInstanceChannel channel;
    private JobInstance currentJobInstance;

    public ParallelJobContextImpl(JobInstance currentJobInstance, JobInstanceChannel channel){
        this.channel = channel;
        this.currentJobInstance = currentJobInstance;
    }

    @Override
    public void distributeSubJob(List<SubJobInstance> subJobList){
        if(subJobInstanceLists == null){
            subJobInstanceLists = new ArrayList<>();
        }

        this.subJobInstanceLists.add(subJobList);
    }


    public boolean uploadSubJobInstances() {
        if(subJobInstanceLists == null || subJobInstanceLists.size() == 0){
            return true;
        }

        int size = 0;
        for(List<SubJobInstance> subJobInstances : subJobInstanceLists){
            size += subJobInstances.size();
        }

        if(size == 0){
            return true;
        }

        List<JobInstance> instances = new ArrayList<>(size);

        subJobInstanceLists.forEach(subJobInstances -> {
            subJobInstances.forEach(subJobInstance -> {
                instances.add(JobInstance.createSubJobInstance(currentJobInstance, subJobInstance.getData(), subJobInstance.getSubJobName()));
            });
        });

        return channel.sendJobInstanceList(instances);
    }

}
