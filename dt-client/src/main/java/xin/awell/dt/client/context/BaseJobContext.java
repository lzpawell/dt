package xin.awell.dt.client.context;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import xin.awell.dt.core.domain.HandleResult;

import java.util.Date;

public abstract class BaseJobContext {

    @Getter
    protected String paras;

    @Getter
    protected String data;

    @Getter
    protected String currentJobName;

    @Getter
    protected Date gmtCreate;

    @Getter
    @Setter
    protected HandleResult result;
}
