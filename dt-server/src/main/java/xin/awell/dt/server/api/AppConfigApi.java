package xin.awell.dt.server.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import xin.awell.dt.core.domain.DataResult;
import xin.awell.dt.server.constant.AppPermissionType;
import xin.awell.dt.server.dao.AppDAO;
import xin.awell.dt.server.dao.queryOption.AppQueryOption;
import xin.awell.dt.server.domain.AppDO;
import xin.awell.dt.server.domain.PermissionDO;
import xin.awell.dt.server.domain.UserDO;
import xin.awell.dt.server.service.ZKService;
import xin.awell.dt.server.utils.AuthUserInfoGetter;

import java.util.*;

/**
 * @author lzp
 * @since 2019/2/242:15
 */
@RestController
@RequestMapping(value = "/api/app")
public class AppConfigApi {
    @Autowired
    private ZKService zkService;

    @Autowired
    private AppDAO appDAO;

    @PostMapping(value = "/delete")
    public DataResult<Void> delete(@RequestParam String appName){
        if(StringUtils.isEmpty(appName)){
            return DataResult.ofFailure(null, "400", "参数不满足条件！");
        }
        if(appDAO.deleteApp(new AppDO(appName, null, null))){
            return DataResult.ofSuccess(null);
        }else{
            return DataResult.ofFailure(null, "500", "服务端异常， 请稍后重试！");
        }
    }

    @PostMapping(value = "/create")
    public DataResult<Void> create(@RequestParam(value = "appName") String appName){
        if(StringUtils.isEmpty(appName)){
            return DataResult.ofFailure(null, "400", "参数不满足条件！");
        }

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String appId = UUID.randomUUID().toString();
        AppDO appDO = new AppDO(
                appName,
                appId,
                Collections.singletonList(new PermissionDO(appId, userDetails.getUsername(), AppPermissionType.APP_OWNER))
        );

        if(appDAO.createApp(appDO)){
            //严格的方式可以走别的方式同步DB 和 zk
            if(zkService.createApp(appId)){
                return DataResult.ofSuccess(null);
            }else{
                appDAO.deleteApp(appDO);
                return DataResult.ofFailure(null, "500", "服务端异常，请稍后重试！");
            }
        }else{
            return DataResult.ofFailure(null, "500", "服务端异常，请稍后重试！");
        }
    }

    @GetMapping("/list")
    private DataResult<List<AppDO>> listApp(){
        Optional<UserDO> userDOOptional = AuthUserInfoGetter.getAuthenticatedUserDO();
        if(userDOOptional.isPresent()){
            String userId = userDOOptional.get().getUserId();
            return DataResult.ofSuccess(appDAO.listAppsForUser(userId, false).orElse(Collections.EMPTY_LIST));
        }else{
            return DataResult.ofFailure(null, "400", "用户异常！");
        }
    }

    @GetMapping("/query")
    private DataResult<AppDO> queryApp(@RequestParam String appName){
        Optional<UserDO> userDOOptional = AuthUserInfoGetter.getAuthenticatedUserDO();
        if(userDOOptional.isPresent()){
            AppDO appDO = appDAO.queryApp(new AppQueryOption()
                    .setUserId(userDOOptional.get().getUserId())
                    .setAppName(appName)
                    .setRequireDetail(true)
            );

            if(appDO != null){
                return DataResult.ofSuccess(appDO);
            }else{
                return DataResult.ofFailure(null, "400", "app不存在！");
            }
        }else{
            return DataResult.ofFailure(null, "400", "服务端异常，请稍后重试！");
        }
    }
}
