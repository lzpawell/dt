package xin.awell.dt.server.dao;

import org.apache.ibatis.annotations.Mapper;
import xin.awell.dt.server.dao.queryOption.AppQueryOption;
import xin.awell.dt.server.domain.AppDO;

/**
 * @author lzp
 * @since 2019/3/2321:03
 */
@Mapper
public interface AppMetaDAO extends BaseDAO<AppDO, AppQueryOption> {
}
