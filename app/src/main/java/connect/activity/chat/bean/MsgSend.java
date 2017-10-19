package connect.activity.chat.bean;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

/**
 * Send chat message
 * Created by gtq on 2016/11/26.
 */
public class MsgSend implements Serializable {

    private LinkMessageRow msgType;
    private Object obj;

    public MsgSend(LinkMessageRow type, Object obj) {
        this.msgType = type;
        this.obj = obj;
    }

    public static void sendOuterMsg(LinkMessageRow type, Object... objs) {
        EventBus.getDefault().post(new MsgSend(type, objs));
    }

    public LinkMessageRow getMsgType() {
        return msgType;
    }

    public Object getObj() {
        return obj;
    }
}