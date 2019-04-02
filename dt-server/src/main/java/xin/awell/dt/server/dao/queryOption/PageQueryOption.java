package xin.awell.dt.server.dao.queryOption;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lzp
 * @since 2019/3/2314:49
 */


public class PageQueryOption {
    @Getter
    private Integer pageSize;

    @Getter
    private Integer pageNum;

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;

        if(this.pageNum != null){
            init();
        }
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;

        if(this.pageSize != null){
            init();
        }
    }

    @Getter
    private Integer offset;
    @Getter
    private Integer length;

    private void init(){
        length = pageSize;
        offset = (pageNum - 1) * pageSize;
    }
}
