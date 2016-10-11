package zjsx.flowlight.bean;

import java.io.Serializable;

/**
 * Created by Admin on 2016/10/11.
 */

public class ConnectedInfo implements Serializable {
    private long masterTime;

    public ConnectedInfo(){
        masterTime = System.currentTimeMillis();
    }

    public long getMasterTime() {
        return masterTime;
    }

    public void setMasterTime(long masterTime) {
        this.masterTime = masterTime;
    }
}
