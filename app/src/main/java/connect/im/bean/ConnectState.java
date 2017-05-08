package connect.im.bean;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

import connect.ui.base.bean.BaseEvent;

/**
 * Created by pujin on 2017/2/6.
 */
public class ConnectState extends BaseEvent {

    public static ConnectState connectState;

    public static ConnectState getInstance() {
        if (connectState == null) {
            connectState = new ConnectState();
        }
        return connectState;
    }

    @Override
    public void sendEvent(Serializable type, Serializable... objects) {
        ConnectState connectState = new ConnectState();
        connectState.setType((ConnectType) type);
        EventBus.getDefault().post(connectState);
    }

    public enum ConnectType {
        DISCONN,
        REFRESH_ING,
        REFRESH_SUCCESS,
        CONNECT,
    }

    private ConnectType type;

    public ConnectState() {
    }

    public ConnectType getType() {
        return type;
    }

    public void setType(ConnectType type) {
        this.type = type;
    }
}
