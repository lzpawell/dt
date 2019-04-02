package xin.awell.dt.server.constant;

/**
 * @author lzp
 * @since 2019/3/2315:08
 */
public enum AppPermissionType implements CodeBaseEnum{
    APP_OWNER(1, "owner"),
    APP_OPS(2, "ops");


    private int code;
    private String name;

    private AppPermissionType(int code,String name){
        this.code = code;
        this.name = name;
    }

    @Override
    public int getCode() {
        return this.code;
    }
}
