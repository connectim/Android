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

    /**
     *
     * @param conversationType
     * @param publicKey
     * @param chatType
     * @param name
     * @param avatar
     * @param timeStamp
     * @param unRead
     * @param content
     * @param draft
     * @param disturb
     * @param top
     * @param at
     * @param stranger
     */
    public void sendConversationEvent(ConversationType conversationType, Serializable publicKey, Serializable chatType,
                                      Serializable name, Serializable avatar, Serializable timeStamp, Serializable unRead,
                                      Serializable content, Serializable draft, Serializable disturb, Serializable top,
                                      Serializable at, Serializable stranger) {
        sendEvent(conversationType, publicKey, chatType, name, avatar, timeStamp, unRead, content, draft, disturb, top, at, stranger);
    }

    public void sendConversationClear() {
        sendEvent(ConversationType.CLEAR);
    }

    public void sendConversationLoad() {
        sendEvent(ConversationType.LOAD);
    }

    public void sendConversationUserInfo(Serializable publicKey, Serializable chatType,
                                         Serializable name, Serializable avatar) {
        sendEvent(ConversationType.UPDATE, publicKey, chatType, name, avatar, -1, -1, "", "", "", -1, -1, -1);
    }

    public void sendConversationSetting(Serializable publicKey, Serializable chatType, Serializable disturb, Serializable top) {
        sendEvent(ConversationType.UPDATE, publicKey, chatType, "", "", -1, -1, "", "", disturb, top, -1, -1);
    }

    @Override
    @Deprecated
    public void sendEvent(Serializable type, Serializable... objects) {
        ConversationAction action = new ConversationAction();
        action.type = (ConversationType) type;
        action.object = objects;
        EventBus.getDefault().post(action);
    }

    private ConversationType type;
    private Object object;

    public ConversationAction() {
    }

    public ConversationType getType() {
        return type;
    }

    public Object getObject() {
        return object;
    }
}
