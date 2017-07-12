package connect.activity.chat.bean;

import connect.im.bean.MsgType;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

/**
 * Send chat message
 * Created by gtq on 2016/11/26.
 */
public class MsgSend implements Serializable {

    private MsgType msgType;
    private Object obj;

    public MsgSend(MsgType type, Object obj) {
        this.msgType = type;
        this.obj = obj;
    }

    public static void sendOuterMsg(MsgType type, Object... objs) {
        EventBus.getDefault().post(new MsgSend(type, objs));
    }

    public MsgType getMsgType() {
        return msgType;
    }

    public Object getObj() {
        return obj;
    }
}