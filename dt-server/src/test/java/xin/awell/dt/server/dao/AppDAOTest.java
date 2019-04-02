package xin.awell.dt.server.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import xin.awell.dt.server.Application;
import xin.awell.dt.server.constant.AppPermissionType;
import xin.awell.dt.server.dao.queryOption.AppQueryOption;
import xin.awell.dt.server.domain.AppDO;
import xin.awell.dt.server.domain.PermissionDO;

import java.util.*;

import static org.junit.Assert.*;

/**
 * @author lzp
 * @since 2019/3/2515:27
 */

@RunWith(SpringRunner.class)
//@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
public class AppDAOTest {

    @Autowired
    private AppDAO appDAO;

    private String tempAppId;


    @Test
    public void test(){
        createApp();

        queryApp();

        updateAppPermission();

        listAppsForUser();

        removeAppPermission();

        deleteApp();
    }

    public void createApp() {
        String appName = "dt-demo";
        UUID uuid = UUID.randomUUID();

        //记录appId以验证query数据
        tempAppId = uuid.toString();
        PermissionDO ownerPermissionDO = new PermissionDO();
        ownerPermissionDO.setAppId(uuid.toString());
        ownerPermissionDO.setPermission(AppPermissionType.APP_OWNER);
        ownerPermissionDO.setUserId("lzp");
        AppDO appDO = new AppDO().setAppId(uuid.toString()).setAppName(appName).setPermissionDOList(Collections.singletonList(ownerPermissionDO));

        assertTrue(appDAO.createApp(appDO));

    }

    public void queryApp() {
        AppDO appDOWithoutDetails = appDAO.queryApp(new AppQueryOption().setRequireDetail(false).setAppName("dt-demo"));

        AppDO appDOWithDetails = appDAO.queryApp(new AppQueryOption().setRequireDetail(true).setAppName("dt-demo"));


        AppDO expectedAppWithDetails = new AppDO()
                .setAppId(tempAppId)
                .setAppName("dt-demo")
                .setPermissionDOList(Collections.singletonList(new PermissionDO(tempAppId, "lzp", AppPermissionType.APP_OWNER)));


        assertEquals(expectedAppWithDetails.getAppId(), appDOWithoutDetails.getAppId());
        assertEquals(expectedAppWithDetails.getAppName(), appDOWithoutDetails.getAppName());
        assertNull(appDOWithoutDetails.getPermissionDOList());


        assertEquals(expectedAppWithDetails, appDOWithDetails);
    }



    public void updateAppPermission() {
        PermissionDO opsPermissionDO = new PermissionDO(tempAppId, "xiaobin", AppPermissionType.APP_OPS);
        assertTrue(appDAO.updateAppPermission(opsPermissionDO));
    }


    public void removeAppPermission() {
        PermissionDO opsPermissionDO = new PermissionDO(tempAppId, "xiaobin", AppPermissionType.APP_OPS);
        assertTrue(appDAO.removeAppPermission(opsPermissionDO));
    }

    public void listAppsForUser() {
        Optional<List<AppDO>> appDOListOptional = appDAO.listAppsForUser("lzp", true);

        assertEquals(Collections.singletonList(
                new AppDO("dt-demo", tempAppId, Arrays.asList(
                        new PermissionDO(tempAppId, "lzp", AppPermissionType.APP_OWNER),
                        new PermissionDO(tempAppId, "xiaobin", AppPermissionType.APP_OPS)
                ))),
                appDOListOptional.orElse(Collections.singletonList(null))
        );
    }


    public void deleteApp() {
        AppDO appDO = new AppDO("dt-demo", null, null);
        assertTrue(appDAO.deleteApp(appDO));
    }
}