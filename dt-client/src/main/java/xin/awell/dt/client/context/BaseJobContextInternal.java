package xin.awell.dt.client.context;

import xin.awell.dt.core.domain.HandleResult;

import javax.xml.crypto.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * @author lzp
 * @since 2019/4/223:25
 */
public interface BaseJobContextInternal {
    void setParas(String paras);

    void setData(Serializable data);

    void setJobName(String jobName);

    void setGmtCreate(Date gmtCreate);

    HandleResult getResult();
}
