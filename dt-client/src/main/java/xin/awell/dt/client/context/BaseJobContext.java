package xin.awell.dt.client.context;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import xin.awell.dt.core.domain.HandleResult;

import java.io.Serializable;
import java.util.Date;

public interface BaseJobContext {

    String getParas();

    Serializable getData();

    String getJobName();

    Date getGmtCreate();

    void setResult(HandleResult result);
}
