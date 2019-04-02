package xin.awell.dt.client.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lzp
 * @since 2019/4/122:08
 */

@Data
public class SubJobInstance {
    private String subJobName;
    private Serializable data;
}
