package zjsx.flowlight.bean;

import java.io.Serializable;

/**
 * Created by Admin on 2016/10/11.
 */

public class MasterInfo implements Serializable{

    private String ip;
    private int port;
    private int group;

    public MasterInfo() {
    }

    public MasterInfo(String ip, int port, int group) {
        this.ip = ip;
        this.port = port;
        this.group = group;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }
}
