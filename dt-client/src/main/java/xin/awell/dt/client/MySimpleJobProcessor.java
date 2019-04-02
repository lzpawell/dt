package xin.awell.dt.client;

import xin.awell.dt.client.context.SimpleJobContext;
import xin.awell.dt.client.domain.TaskResult;
import xin.awell.dt.client.processer.SimpleJobProcessor;

/**
 * @author lzp
 * @since 2019/4/12:16
 */
public class MySimpleJobProcessor implements SimpleJobProcessor {
    @Override
    public TaskResult process(SimpleJobContext taskContext) {
        return null;
    }
}
