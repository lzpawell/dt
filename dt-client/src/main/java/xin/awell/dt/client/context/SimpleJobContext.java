package xin.awell.dt.client.context;

import xin.awell.dt.core.domain.HandleResult;

import java.io.Serializable;
import java.util.Date;

public class SimpleJobContext extends BaseJobContextImpl {

    public SimpleJobContext(String paras, Serializable data, String jobName, Date gmtCreate, HandleResult result) {
        super(paras, data, jobName, gmtCreate, result);
    }
}
