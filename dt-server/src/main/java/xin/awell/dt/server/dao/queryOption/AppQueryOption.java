package xin.awell.dt.server.dao.queryOption;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author lzp
 * @since 2019/3/2314:56
 */
@Data
@Accessors(chain = true)
public class AppQueryOption extends PageQueryOption {
    private String appName;
    private String appId;
    /**
     * app ops的id， 设置这个参数可以查询这个用户相关的app
     */
    private String userId;

    private boolean requireDetail;
}
