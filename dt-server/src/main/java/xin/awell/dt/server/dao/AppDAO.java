package xin.awell.dt.server.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.assertj.core.util.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import xin.awell.dt.server.dao.queryOption.AppQueryOption;
import xin.awell.dt.server.domain.AppDO;
import xin.awell.dt.server.domain.PermissionDO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author lzp
 * @since 2019/3/2122:33
 */
@Repository
@Slf4j
public class AppDAO {
    @Autowired
    private AppACLDAO appACLDAO;

    @Autowired
    private AppMetaDAO appMetaDAO;

    @Autowired
    private DataSourceTransactionManager transactionManager;

    /**
     * 手动事务处理, 以实现在函数内打log
     * @param appDO
     * @return
     */
    public boolean createApp(AppDO appDO){
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = transactionManager.getTransaction(def);

        try{
            appMetaDAO.insert(appDO);
            Optional.ofNullable(appDO.getPermissionDOList())
                    .ifPresent(permissionDOList -> {
                        permissionDOList.forEach(permissionDO -> appACLDAO.insertOrUpdate(permissionDO));
                    });

            transactionManager.commit(status);
            return true;
        }catch (Exception e){
            log.error("create app failure! msg : {}", e.getMessage());
            log.error("stack trace: ", e);
            transactionManager.rollback(status);

            return false;
        }
    }

    public AppDO queryApp(AppQueryOption option){
        AppDO appDO = null;
        if(option.getAppName() != null){
            appDO = appMetaDAO.selectOne(option);
        }

        AppQueryOption newOption = new AppQueryOption();
        BeanUtils.copyProperties(option, newOption);
        newOption.setAppId(appDO.getAppId());
        if(newOption.isRequireDetail()){
            appDO.setPermissionDOList(appACLDAO.query(newOption));
        }

        return appDO;
    }

    @Transactional
    public boolean deleteApp(AppDO appDO){
        AppQueryOption option = new AppQueryOption().setAppName(appDO.getAppName());
        appDO = appMetaDAO.selectOne(option);
        appACLDAO.deleteApp(appDO.getAppId());
        appMetaDAO.delete(appDO);
        return true;
    }

    public boolean updateAppPermission(PermissionDO permissionDO){
        return appACLDAO.insertOrUpdate(permissionDO) > 0;
    }

    public boolean removeAppPermission(PermissionDO permissionDO){
        return appACLDAO.delete(permissionDO) > 0;
    }

    public Optional<List<AppDO>> listAppsForUser(String userId, boolean requireDetails){
        List<PermissionDO> permissionDOList = appACLDAO.query(new AppQueryOption().setUserId(userId));



        if(permissionDOList != null){
            List<AppDO> appDOList = permissionDOList.stream().map(permissionDO -> appMetaDAO.selectOne(new AppQueryOption().setAppId(permissionDO.getAppId()))).collect(Collectors.toList());
            if(requireDetails){
                appDOList.forEach(appDO -> {
                    List<PermissionDO> subPermissions = appACLDAO.query(new AppQueryOption().setAppId(appDO.getAppId()));
                    appDO.setPermissionDOList(subPermissions);
                });
            }

            return Optional.of(appDOList);
        }

        return Optional.empty();
    }
}
