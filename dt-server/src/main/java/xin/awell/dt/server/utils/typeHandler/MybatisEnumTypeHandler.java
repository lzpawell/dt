package xin.awell.dt.server.utils.typeHandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import xin.awell.dt.server.constant.CodeBaseEnum;

import java.lang.reflect.ParameterizedType;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author lzp
 * @since 2019/3/2316:56
 */
public class MybatisEnumTypeHandler<E extends Enum<?> & CodeBaseEnum> extends BaseTypeHandler<CodeBaseEnum> {

    private Class<E> clazz;

    public MybatisEnumTypeHandler(){
        ParameterizedType type = (ParameterizedType)this.getClass().getGenericSuperclass();
        clazz = (Class<E>) type.getActualTypeArguments()[0];
    }

    public MybatisEnumTypeHandler(Class<E> enumType) {
        if (enumType == null){
            throw new IllegalArgumentException("Type argument cannot be null");
        }

        this.clazz = enumType;
    }

    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, CodeBaseEnum codeBaseEnum, JdbcType jdbcType) throws SQLException {
        preparedStatement.setInt(i, codeBaseEnum.getCode());
    }

    @Override
    public E getNullableResult(ResultSet resultSet, String s) throws SQLException {
        int code = resultSet.getInt(s);
        return getEnumByCode(code);
    }

    @Override
    public E getNullableResult(ResultSet resultSet, int i) throws SQLException {
        int code = resultSet.getInt(i);
        return getEnumByCode(code);
    }

    @Override
    public E getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        int code = callableStatement.getInt(i);
        return getEnumByCode(code);
    }

    private E getEnumByCode(int code){
        for(E e : clazz.getEnumConstants()){
            if(e.getCode() == code){
                return e;
            }
        }

        return null;
    }
}
