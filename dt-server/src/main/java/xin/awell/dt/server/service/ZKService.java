package xin.awell.dt.server.service;

import xin.awell.dt.core.domain.DataResult;
import xin.awell.dt.core.domain.JobConfigDO;

import java.util.List;

/**
 * @author lzp
 * @since 2019/2/2323:10
 */
public interface ZKService {
    List<JobConfigDO> listConfigures(String appId) throws Exception;
    boolean setJob(JobConfigDO data);
    boolean deleteJob(String appId, String jobId);
    boolean deleteApp(String appId);
    boolean createApp(String appId);
}
