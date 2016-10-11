package zjsx.flowlight.bean;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by Admin on 2016/10/11.
 */

public class IPInfo implements Serializable{

    public static final IPInfo INSTANCE = new IPInfo();

    private String uuid;

    public IPInfo(){
        uuid = UUID.randomUUID().toString();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
