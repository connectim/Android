package connect.ui.activity.home.bean;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

/**
 * refresh fragment
 * Created by gtq on 2016/12/4.
 */
public class MsgFragmReceiver implements Serializable{

    public enum FragRecType{
        ALL,
        ROOM,
    }

    public FragRecType fragRecType;
    public Object object;

    public MsgFragmReceiver(FragRecType fragRecType, Object... objects) {
        this.fragRecType = fragRecType;
        this.object = objects;
    }

    public static void refreshRoom(FragRecType type,Object... objects){
        EventBus.getDefault().post(new MsgFragmReceiver(type, objects));
    }
}
