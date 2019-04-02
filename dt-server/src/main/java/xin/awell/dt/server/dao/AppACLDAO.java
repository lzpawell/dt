package xin.awell.dt.server.dao;

import org.apache.ibatis.annotations.Mapper;
import xin.awell.dt.server.dao.queryOption.AppQueryOption;
import xin.awell.dt.server.domain.PermissionDO;

import java.util.List;

/**
 * @author lzp
 * @since 2019/3/2321:04
 */
@Mapper
public interface AppACLDAO extends BaseDAO<PermissionDO, AppQueryOption>{
    int deleteApp(String appId);
    int insertOrUpdate(PermissionDO permissionDO);
}
