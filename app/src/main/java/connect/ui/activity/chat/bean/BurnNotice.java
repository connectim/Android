package connect.ui.activity.chat.bean;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

/**
 * Created by gtq on 2016/12/13.
 */
public class BurnNotice implements Serializable {
    private BurnType burnType;
    private Object objs;

    public BurnNotice(BurnType burnType, Object objs) {
        this.burnType = burnType;
        this.objs = objs;
    }

    public enum BurnType {
        BURN_START,//Open burn after reading
        BURN_READ,//After reading the news has been read
    }

    public static void sendBurnMsg(BurnType type, Object... obj) {
        EventBus.getDefault().post(new BurnNotice(type, obj));
    }

    public BurnType getBurnType() {
        return burnType;
    }

    public Object getObjs() {
        return objs;
    }
}
