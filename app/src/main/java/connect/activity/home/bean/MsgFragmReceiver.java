package connect.activity.home.bean;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

/**
 * refresh fragment
 */
public class MsgFragmReceiver implements Serializable {

    public static void refreshRoom() {
        EventBus.getDefault().post(new MsgFragmReceiver());
    }
}
