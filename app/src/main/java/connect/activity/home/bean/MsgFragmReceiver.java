package connect.activity.home.bean;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

/**
 * refresh fragment
 * Created by gtq on 2016/12/4.
 */
public class MsgFragmReceiver implements Serializable {

    public static void refreshRoom() {
        EventBus.getDefault().post(new MsgFragmReceiver());
    }
}
