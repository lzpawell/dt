package xin.awell.dt.server.processor;

import com.alibaba.fastjson.JSON;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import xin.awell.dt.client.context.SimpleJobContext;
import xin.awell.dt.client.processer.SimpleJobProcessor;

/**
 * @author lzp
 * @since 2019/4/12:16
 */
public class MySimpleJobProcessor implements SimpleJobProcessor {
    @Override
    public void process(SimpleJobContext jobContext) {
        System.out.println("处理简单工作！" + JSON.toJSONString(jobContext));
    }
}
