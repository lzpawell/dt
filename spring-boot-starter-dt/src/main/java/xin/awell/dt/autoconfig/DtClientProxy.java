package xin.awell.dt.autoconfig;

import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import xin.awell.dt.client.core.DtClient;

/**
 * @author lzp
 * @since 2019/2/2822:33
 */
@Slf4j
public class DtClientProxy implements InitializingBean, DisposableBean {

    public DtClientProxy(DtConfig config){
        this.dtConfig = config;
    }

    private DtConfig dtConfig;

    private DtClient dtClient;

    @Override
    public void destroy() throws Exception {
        try {
            dtClient.shutdown();
            log.info("dt client shutdown! ");
        }catch (Exception e){
            log.error("dt client shutdown error! ", e);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String ZK_CONNECTION_STR = "39.108.65.230:2181";
        dtClient = new DtClient(ZK_CONNECTION_STR, dtConfig.getAppId());
        dtClient.start();
    }
}
