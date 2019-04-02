package xin.awell.dt.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import xin.awell.dt.server.dao.UserDAO;
import xin.awell.dt.server.domain.UserDO;

/**
 * @author lzp
 * @since 2019/3/2620:49
 */
@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    @Autowired
    private UserDAO userDAO;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userDetails = null;
        if(!StringUtils.isEmpty(username)){
            userDetails = userDAO.selectOne(new UserDO(username, null));
        }

        if(userDetails != null){
            return userDetails;
        }else{
            throw new UsernameNotFoundException(username);
        }
    }
}
