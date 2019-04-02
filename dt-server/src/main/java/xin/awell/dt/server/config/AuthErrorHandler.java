package xin.awell.dt.server.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import xin.awell.dt.core.domain.DataResult;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author lzp
 * @since 2019/3/2622:32
 */
public class AuthErrorHandler implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        DataResult errResult = null;
        if(e instanceof UsernameNotFoundException){
            errResult = DataResult.ofFailure(null, "400", "用户名不存在");
        }else{
            errResult = DataResult.ofFailure(null, "500", "服务端异常， 请稍后重试！");
        }

        httpServletResponse.setHeader("Content-Type", "application/json; charset=UTF-8");
        httpServletResponse.getWriter().println(JSON.toJSONString(errResult));
    }
}
