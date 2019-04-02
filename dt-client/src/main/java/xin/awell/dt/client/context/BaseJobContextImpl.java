package xin.awell.dt.client.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import xin.awell.dt.core.domain.HandleResult;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lzp
 * @since 2019/4/222:10
 */
@AllArgsConstructor
public class BaseJobContextImpl implements BaseJobContext, BaseJobContextInternal{

    @Getter
    protected String paras;

    @Getter
    protected Serializable data;

    @Getter
    protected String jobName;

    @Getter
    protected Date gmtCreate;

    @Getter
    @Setter
    protected HandleResult result;
}
