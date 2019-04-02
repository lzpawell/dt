package xin.awell.dt.autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;

/**
 * @author lzp
 * @since 2019/2/2822:29
 */
@Configuration
@EnableConfigurationProperties(DtConfig.class)
@ConditionalOnProperty(prefix = "xin.awell.dt.enable", value = "true", matchIfMissing = true)
public class DtAutoConfiguration {

    @Bean
    public DtClientProxy getDtClientProxy(DtConfig config){
        Objects.requireNonNull(config);
        Objects.requireNonNull(config.getAppId());
        Objects.requireNonNull(config.getAppName());
        return new DtClientProxy(config);
    }
}
