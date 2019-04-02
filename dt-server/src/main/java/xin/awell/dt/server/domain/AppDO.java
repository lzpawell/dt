package xin.awell.dt.server.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Objects;

/**
 * @author lzp
 * @since 2019/3/2122:39
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class AppDO {
    private String appName;
    private String appId;
    private List<PermissionDO> permissionDOList;
}
