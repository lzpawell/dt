package xin.awell.dt.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import xin.awell.dt.server.api.AppConfigApi;
import xin.awell.dt.server.service.ZKService;

import java.util.stream.Stream;

/**
 * @author lzp
 * @since 2019/2/2322:47
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Application.class, args);
    }
}
