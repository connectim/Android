package connect.ui.activity.chat.bean;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

public class MsgChatReceiver implements Serializable {
    private String pubKey;
    private BaseEntity bean;

    public MsgChatReceiver() {
    }

    public MsgChatReceiver(BaseEntity bean) {
        this.bean = bean;
    }

    public static void sendChatReceiver(String roomkey,BaseEntity entity){
        MsgChatReceiver receiver = new MsgChatReceiver();
        receiver.setPubKey(roomkey);
        receiver.setBean(entity);
        EventBus.getDefault().post(receiver);
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public BaseEntity getBean() {
        return bean;
    }

    public void setBean(BaseEntity bean) {
        this.bean = bean;
    }
}