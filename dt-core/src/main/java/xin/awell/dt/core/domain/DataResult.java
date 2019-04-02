package xin.awell.dt.core.domain;

import lombok.Data;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * @author lzp
 * @since 2019/2/2322:51
 */
@Data
@Accessors(chain = true)
@ToString
public class DataResult<T> {
    private T obj;
    private boolean success;
    private String errMsg;
    private String errCode;

    private DataResult(T obj, boolean success, String errCode, String errMsg){
        this.obj = obj;
        this.success = success;
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

     public static <T> DataResult<T> ofSuccess(T obj){
        return new DataResult<>(obj, true, null, null);
    }

    public static <T> DataResult<T> ofFailure(T obj,@NonNull String errCode, @NonNull String errMsg){
        return new DataResult<>(obj, false, errCode, errMsg);
    }
}
