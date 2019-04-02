package xin.awell.dt.server.api;

import org.springframework.web.bind.annotation.*;
import xin.awell.dt.core.domain.DataResult;

/**
 * @author lzp
 * @since 2019/2/2418:57
 */
@RestController
@RequestMapping(value = "/api/user")
public class UserAuthApi {
    @RequestMapping(value = "/login/{type}", method = { RequestMethod.GET, RequestMethod.POST})
    public DataResult<Void> login(@PathVariable(value = "type", required = false) String type){
        if("failure".equals(type)){
            return DataResult.ofFailure(null, "400", "balala");
        }else if("success".equals(type)){
            return DataResult.ofSuccess(null);
        }

        return null;
    }

    @PostMapping(value = "/register")
    public DataResult<Void> register(){
        return null;
    }
}
