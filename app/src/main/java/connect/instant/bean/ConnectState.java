package connect.instant.bean;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

/**
 * Created by pujin on 2017/2/6.
 */
public class ConnectState {

    public static ConnectState connectState;

    public static ConnectState getInstance() {
        if (connectState == null) {
            connectState = new ConnectState();
        }
        return connectState;
    }

    public void sendEvent(Serializable type) {
        ConnectState connectState = new ConnectState();
        connectState.setType((ConnectType) type);
        EventBus.getDefault().post(connectState);
    }

    public enum ConnectType {
        DISCONN,
        REFRESH_ING,
        REFRESH_SUCCESS,
        CONNECT,
        OFFLINE_PULL,
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
