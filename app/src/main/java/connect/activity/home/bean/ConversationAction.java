package connect.activity.home.bean;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

import connect.activity.base.bean.BaseEvent;

/**
 * Refresh Conversation Fragment
 */
public class ConversationAction extends BaseEvent {

    public static ConversationAction conversationAction = getInstance();

    private synchronized static ConversationAction getInstance() {
        if (conversationAction == null) {
            conversationAction = new ConversationAction(ConverType.LOAD_MESSAGE);
        }
        return conversationAction;
    }

    public enum ConverType {
        LOAD_MESSAGE,
        LOAD_UNREAD,
    }

    private ConverType converType;

    public ConversationAction(ConverType type) {
        this.converType = type;
    }

    public void sendEvent(ConverType type) {
        this.converType = type;
        sendEvent(type, "");
    }

    @Override
    public void sendEvent(Serializable type, Serializable... objects) {
        ConversationAction action = new ConversationAction((ConverType)type);
        EventBus.getDefault().post(action);
    }

    public ConverType getConverType() {
        return converType;
    }
}
