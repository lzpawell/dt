package xin.awell.dt.server.api;

import org.springframework.web.bind.annotation.*;
import xin.awell.dt.core.domain.DataResult;
import xin.awell.dt.server.domain.UserDO;

import java.util.Map;

/**
 * @author lzp
 * @since 2019/3/272:55
 */
@RestController
@RequestMapping("/test")
public class TestApi {
    @PostMapping("/balala")
    public DataResult<Void> test(@RequestBody UserDO input){
        System.out.println(input);
        return DataResult.ofSuccess(null);
    }
}
