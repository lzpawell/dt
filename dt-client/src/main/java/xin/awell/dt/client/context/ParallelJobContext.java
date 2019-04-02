package xin.awell.dt.client.context;

import xin.awell.dt.client.core.JobInstanceChannel;
import xin.awell.dt.client.domain.SubJobInstance;
import xin.awell.dt.client.processer.ParallelJobProcessor;
import xin.awell.dt.core.domain.HandleResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author lzp
 * @since 2018/11/2715:38
 */
public abstract class ParallelJobContext extends BaseJobContextImpl {
    public ParallelJobContext(String paras, Serializable data, String jobName, Date gmtCreate, HandleResult result) {
        super(paras, data, jobName, gmtCreate, result);
    }

    public abstract void distributeSubJob(List<SubJobInstance> subJobList);
}
