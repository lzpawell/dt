package xin.awell.dt.client.processer;

import xin.awell.dt.client.context.BaseJobContext;

/**
 * @author lzp
 * @since 2018/11/2715:49
 */
public interface BaseJobProcessor<T extends BaseJobContext> {
    void process(T taskContext);
}
