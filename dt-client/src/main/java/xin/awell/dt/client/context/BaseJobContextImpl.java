package xin.awell.dt.client.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import xin.awell.dt.core.domain.HandleResult;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lzp
 * @since 2019/4/222:10
 */
@Data
@AllArgsConstructor
public class BaseJobContextImpl implements BaseJobContext, BaseJobContextInternal{
    protected String paras;
    protected Serializable data;
    protected String jobName;
    protected Date gmtCreate;
    protected HandleResult result;
}
