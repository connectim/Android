package connect.activity.chat.bean;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

import connect.activity.base.bean.BaseEvent;

/**
 * Created by Administrator on 2017/8/22.
 */

public class DestructReadBean extends BaseEvent implements Serializable {

    private String messageId;

    public static DestructReadBean readBean;

    public static DestructReadBean getInstance() {
        if (readBean == null) {
            synchronized (DestructReadBean.class) {
                if (readBean == null) {
                    readBean = new DestructReadBean();
                }
            }
        }
        return readBean;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public void sendEvent(Serializable type, Serializable... objects) {
        DestructReadBean readBean = new DestructReadBean();
        readBean.setMessageId((String)type);
        EventBus.getDefault().post(readBean);
    }
}
