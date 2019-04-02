package xin.awell.dt.server.config;

import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author lzp
 * @since 2019/2/240:45
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "zk.config")
public class ZKConfig {
    private String connectionString;
}
