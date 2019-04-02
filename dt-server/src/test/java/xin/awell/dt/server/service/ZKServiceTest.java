package xin.awell.dt.server.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import xin.awell.dt.core.domain.DataResult;
import xin.awell.dt.server.Application;


/**
 * @author lzp
 * @since 2019/3/1921:23
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
public class ZKServiceTest {

    @Autowired //自动注入
    private ZKService zkService;

    @Test
    public void createApp() {
        boolean result = zkService.createApp("balala");

    }


    @Test
    public void listConfigures() {

    }

    @Test
    public void setJob() {
    }

    @Test
    public void deleteJob() {
    }

    @Test
    public void deleteApp() {
    }


}