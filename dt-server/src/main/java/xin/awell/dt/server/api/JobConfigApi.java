package xin.awell.dt.server.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import xin.awell.dt.core.domain.DataResult;
import xin.awell.dt.core.domain.JobConfigDO;
import xin.awell.dt.server.dao.AppDAO;
import xin.awell.dt.server.dao.queryOption.AppQueryOption;
import xin.awell.dt.server.domain.AppDO;
import xin.awell.dt.server.service.ZKService;

import java.util.List;
import java.util.UUID;

/**
 * @author lzp
 * @since 2019/2/2322:50
 */
@RestController
@RequestMapping(value = "/api/job")
public class JobConfigApi {
    @Autowired
    private ZKService zkService;

    @Autowired
    private AppDAO appDAO;

    @PostMapping(value = {"/create", "/update"})
    public DataResult<Void> createOrUpdate(JobConfigDO config){
        if(config.getJobId() == null){
            //create job
            config.setJobId(UUID.randomUUID().toString());
            config.setEnable(false);
        }

        if(zkService.setJob(config)){
            return DataResult.ofSuccess(null);
        }else{
            return DataResult.ofFailure(null, "500", "服务器异常， 请稍后重试！");
        }
    }

    @PostMapping(value = "/delete")
    public DataResult<Void> delete(@RequestParam String appId, @RequestParam String jobId){

        if(zkService.deleteJob(appId, jobId)){
            return DataResult.ofSuccess(null);
        }else{
            return DataResult.ofFailure(null, "500", "服务器异常， 请稍后重试！");
        }
    }

    @GetMapping(value = "/list")
    public DataResult<List<JobConfigDO>> list(@RequestParam String appName){
        try{
            AppDO appDO = appDAO.queryApp(new AppQueryOption().setAppName(appName));
            return DataResult.ofSuccess(zkService.listConfigures(appDO.getAppId()));
        }catch (Exception e){
            return DataResult.ofFailure(null, "500", "服务器异常， 请稍后重试！");
        }
    }

    @PostMapping(value = "linkJobs")
    public DataResult<Void> linkJobs(@RequestParam("prefixJob") String prefixJobId, @RequestParam("") String postfixJobId){
        return null;
    }

    @PostMapping(value = "deteachJobs")
    public DataResult<Void> deteachJobs(@RequestParam("prefixJob") String prefixJobId, @RequestParam("") String postfixJobId){
        return null;
    }
}
