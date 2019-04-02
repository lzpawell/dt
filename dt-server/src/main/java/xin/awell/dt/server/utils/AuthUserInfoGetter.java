package xin.awell.dt.server.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import xin.awell.dt.server.domain.UserDO;

import java.util.Optional;

/**
 * @author lzp
 * @since 2019/3/292:27
 */
public class AuthUserInfoGetter {
    public static Optional<UserDO> getAuthenticatedUserDO(){
        SecurityContext context = SecurityContextHolder.getContext();
        return Optional.ofNullable(context.getAuthentication()).map(Authentication::getPrincipal).map(principal -> (UserDO) principal);
    }
}
