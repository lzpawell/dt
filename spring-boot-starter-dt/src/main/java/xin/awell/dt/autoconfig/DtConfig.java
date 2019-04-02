package xin.awell.dt.autoconfig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lzp
 * @since 2019/2/2822:28
 */
@Data
@ConfigurationProperties(prefix = "xin.awell.dt")
public class DtConfig {
    private String appName;
    private String appId;
    private Boolean enable;
}
