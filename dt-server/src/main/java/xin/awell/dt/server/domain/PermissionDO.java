package xin.awell.dt.server.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import xin.awell.dt.server.constant.AppPermissionType;

/**
 * @author lzp
 * @since 2019/3/2315:07
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PermissionDO {
    private String appId;
    private String userId;
    private AppPermissionType permission;
}
