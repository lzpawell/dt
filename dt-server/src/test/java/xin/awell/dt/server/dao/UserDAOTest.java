package xin.awell.dt.server.dao;

import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import xin.awell.dt.server.Application;
import xin.awell.dt.server.domain.UserDO;

import javax.validation.constraints.AssertTrue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author lzp
 * @since 2019/3/2122:18
 */

@RunWith(SpringRunner.class)
//@ActiveProfiles("test")
@SpringBootTest(classes = Application.class)
public class UserDAOTest {

    @Autowired
    private UserDAO userDAO;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    public void insert() {
        UserDO userDO = new UserDO("lzp", passwordEncoder.encode("chuyin12345"));
        UserDO xiaobin = new UserDO("xiaobin", passwordEncoder.encode("xiaobin"));
        Assert.assertEquals(1, userDAO.insert(xiaobin));
        Assert.assertEquals(1, userDAO.insert(userDO));
    }


    @Test
    public void queryAll() {
        UserDO awell = new UserDO("lzp", passwordEncoder.encode("chuyin12345"));
        UserDO xiaobin = new UserDO("xiaobin", passwordEncoder.encode("xiaobin"));

        List<UserDO> actualList = userDAO.query(new UserDO(null, null));

        actualList.forEach(userDO -> {
            switch (userDO.getUserId()){
                case "lzp":
                    Assert.assertTrue(passwordEncoder.matches("chuyin12345",userDO.getPassword()));
                    break;
                case "xiaobin":
                    Assert.assertTrue(passwordEncoder.matches("xiaobin",userDO.getPassword()));
                    break;
                    default: fail("数据不一致！");
            }
        });
    }

    @Test
    public void queryByUserId() {
        UserDO userDO = userDAO.selectOne(new UserDO("lzp", null));

        Assert.assertEquals(userDO.getUserId(), "lzp");
        Assert.assertTrue(passwordEncoder.matches("chuyin12345", userDO.getPassword()));
    }

    @Test
    public void deleteByUserId() {
        Assert.assertEquals(1, userDAO.delete(new UserDO("lzp", null)));
        Assert.assertEquals(1, userDAO.delete(new UserDO("xiaobin", null)));
    }
}