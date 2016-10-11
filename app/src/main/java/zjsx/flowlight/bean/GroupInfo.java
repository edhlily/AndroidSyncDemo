package zjsx.flowlight.bean;

import java.io.Serializable;

/**
 * Created by Admin on 2016/10/11.
 */

public class GroupInfo implements Serializable{
    private int group;

    public GroupInfo() {

    }

    public GroupInfo(int group) {
        this.group = group;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }
}
