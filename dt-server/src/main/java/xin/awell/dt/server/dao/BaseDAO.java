package xin.awell.dt.server.dao;

import java.util.Collection;
import java.util.List;

/**
 * @author lzp
 * @since 2019/3/2314:17
 */
public interface BaseDAO <DO, QueryOpt> {
    int insert(DO t);
    int insertAll(Collection<DO> list);
    int delete(DO t);
    int deleteAll(Collection<DO> list);
    List<DO> query(QueryOpt queryOption);
    DO selectOne(QueryOpt queryOption);
    int count(QueryOpt queryOption);
    int update(DO t);
}
