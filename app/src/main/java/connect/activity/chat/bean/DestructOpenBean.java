package connect.activity.chat.bean;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

/**
 * Destruct Message open state
 * Created by Administrator on 2017/8/22.
 */
public class DestructOpenBean implements Serializable {

    private long time;

    public DestructOpenBean(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public static void sendDestructMsg(long time) {
        EventBus.getDefault().post(new DestructOpenBean(time));
    }
}
