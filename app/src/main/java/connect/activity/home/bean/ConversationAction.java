package connect.activity.home.bean;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

import connect.activity.base.bean.BaseEvent;

/**
 * Refresh Conversation Fragment
 */
public class ConversationAction extends BaseEvent {

    public static ConversationAction conversationAction = getInstance();

    private static ConversationAction getInstance() {
        if (conversationAction == null) {
            synchronized (ConversationAction.class) {
                if (conversationAction == null) {
                    conversationAction = new ConversationAction();
                }
            }
        }
        return conversationAction;
    }

    public void sendEvent() {
        sendEvent("","");
    }

    @Override
    public void sendEvent(Serializable type, Serializable... objects) {
        ConversationAction action = new ConversationAction();
        EventBus.getDefault().post(action);
    }
}
