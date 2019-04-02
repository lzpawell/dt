package xin.awell.dt.client.context;

import xin.awell.dt.core.domain.HandleResult;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lzp
 * @since 2019/4/12:38
 */
public class CommonJobContext extends BaseJobContextImpl {
    public CommonJobContext(String paras, Serializable data, String jobName, Date gmtCreate, HandleResult result) {
        super(paras, data, jobName, gmtCreate, result);
    }
}
