package xin.awell.dt.client.processer;

import xin.awell.dt.client.context.SimpleJobContext;

/**
 * @author lzp
 * @since 2018/11/2816:35
 *
 * 集群task， task触发后整个集群所有实例都会触发一次这个回调
 */
public interface CommonJobProcessor extends BaseJobProcessor<SimpleJobContext> {
}
