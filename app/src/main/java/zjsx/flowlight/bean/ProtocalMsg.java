package zjsx.flowlight.bean;

import java.io.Serializable;

/**
 * Created by Admin on 2016/10/11.
 */

public class ProtocalMsg implements Serializable{
    private String ip;
    private int code;
    private String msg;

    public ProtocalMsg() {
    }

    public ProtocalMsg(String ip,int code, String msg) {
        this.ip = ip;
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
