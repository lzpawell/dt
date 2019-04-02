package xin.awell.dt.core.domain;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author lzp
 * @since 2018/11/2715:42
 */
@Data
@Accessors(chain = true)
public class HandleResult {
    private boolean success;
    private boolean requireRetry;
    private boolean requireRemoteRetry;

    public static final HandleResult SUCCESS = new HandleResult().setSuccess(true);


    public static HandleResult handleFailure(boolean requireRetry, boolean requireRemoteRetry){
        HandleResult result = new HandleResult().setSuccess(false).setRequireRetry(requireRetry);

        if(requireRetry){
            result.setRequireRemoteRetry(requireRemoteRetry);
        }

        return result;
    }
}
