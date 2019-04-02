package xin.awell.dt.client.context;

import xin.awell.dt.client.core.JobInstanceChannel;
import xin.awell.dt.client.domain.SubJobInstance;
import xin.awell.dt.client.processer.ParallelJobProcessor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lzp
 * @since 2018/11/2715:38
 */
public abstract class ParallelJobContext extends BaseJobContext {
    public abstract void distributeSubJob(List<SubJobInstance> subJobList);
}
