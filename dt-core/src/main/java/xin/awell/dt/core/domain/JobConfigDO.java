package xin.awell.dt.core.domain;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import xin.awell.dt.core.constant.JobPriority;
import xin.awell.dt.core.constant.JobType;
import xin.awell.dt.core.constant.TriggerMode;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * @author lzp
 * @since 2019/2/191:20
 */

@Data
@Accessors(chain = true)
public class JobConfigDO {
    private String jobId;
    private String appId;
    private String jobDesc;
    private String jobProcessor;
    private TriggerMode triggerMode;
    private String cronExp;
    private Boolean enable;
    private String runtimeParas;
    private JobType jobType;
    private JobPriority priority;

    @Override
    public int hashCode() {
        return jobId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == null){
            return false;
        }
        if(obj instanceof JobConfigDO){
            Class clazz = JobConfigDO.class;
            Field[] fields = clazz.getDeclaredFields();

            boolean[] equalsWrapper = {true};
            Stream.of(fields).forEach(field -> {
                field.setAccessible(true);
                try {
                    if(!Objects.equals(field.get(this), field.get(obj))){
                        equalsWrapper[0] = false;
                    }
                } catch (IllegalAccessException e) {
                    equalsWrapper[0] = false;
                }
            });

            return equalsWrapper[0];
        }else{
            return false;
        }
    }
}
