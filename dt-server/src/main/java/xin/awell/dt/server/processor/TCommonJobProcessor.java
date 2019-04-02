package xin.awell.dt.server.processor;

import com.alibaba.fastjson.JSON;
import xin.awell.dt.client.context.CommonJobContext;
import xin.awell.dt.client.processer.CommonJobProcessor;

/**
 * @author lzp
 * @since 2019/4/12:16
 */
public class TCommonJobProcessor implements CommonJobProcessor {
    @Override
    public void process(CommonJobContext jobContext) {
        System.out.println("run common job!" + JSON.toJSONString(jobContext));
    }
}
