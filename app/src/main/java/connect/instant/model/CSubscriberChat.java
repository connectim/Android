package connect.instant.model;

import android.text.TextUtils;

import java.util.List;

import connect.activity.home.bean.ConversationAction;
import connect.activity.home.bean.RoomAttrBean;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.SubscribeConversationHelper;
import connect.database.green.bean.ConversionEntity;
import connect.database.green.bean.SubscribeConversationEntity;
import connect.instant.inter.ConversationListener;

/**
 * Created by Administrator on 2017/11/22.
 */

public class CSubscriberChat extends SubscriberChat implements ConversationListener {

    public static CSubscriberChat cSubscriberChat = getInstance();

    private synchronized static CSubscriberChat getInstance() {
        if (cSubscriberChat == null) {
            cSubscriberChat = new CSubscriberChat();
        }
        return cSubscriberChat;
    }

    public void updateRoomMsg(String draft, String showText, long msgtime) {
        updateRoomMsg(draft, showText, msgtime, -1);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at) {
        updateRoomMsg(draft, showText, msgtime, at, 0);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg) {
        updateRoomMsg(draft, showText, msgtime, at, newmsg, true);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg, boolean broad) {
        if (TextUtils.isEmpty(chatKey())) {
            return;
        }

        List<RoomAttrBean> roomEntities = ConversionHelper.getInstance().loadRoomEntities(chatKey());
        if (roomEntities == null || roomEntities.size() == 0) {
            ConversionEntity conversionEntity = new ConversionEntity();
            conversionEntity.setIdentifier(chatKey());
            conversionEntity.setName(nickName());
            conversionEntity.setAvatar(headImg());
            conversionEntity.setType(chatType());
            conversionEntity.setContent(TextUtils.isEmpty(showText) ? "" : showText);
            conversionEntity.setStranger(isStranger ? 1 : 0);
            conversionEntity.setUnread_count(newmsg == 0 ? 0 : 1);
            conversionEntity.setDraft(TextUtils.isEmpty(draft) ? "" : draft);
            conversionEntity.setIsAt(at);
            ConversionHelper.getInstance().insertRoomEntity(conversionEntity);
        } else {
            for (RoomAttrBean attrBean : roomEntities) {
                ConversionHelper.getInstance().updateRoomEntity(
                        chatKey(),
                        TextUtils.isEmpty(draft) ? "" : draft,
                        TextUtils.isEmpty(showText) ? attrBean.getContent() : showText,
                        (newmsg <= 0 ? 0 : 1 + attrBean.getUnread()),
                        at,
                        (isStranger ? 1 : 0),
                        (msgtime <= 0 ? attrBean.getTimestamp() : msgtime)
                );
            }
        }
        if (broad) {
            ConversationAction.conversationAction.sendEvent(ConversationAction.ConverType.LOAD_MESSAGE);
        }
    }

    public void updateConversationListEntity(long rssId, String icon, String title, String showText, long msgtime, int newmsg) {
        updateRoomMsg("", "", msgtime, -1);

        List<SubscribeConversationEntity> conversationEntities = SubscribeConversationHelper.subscribeConversationHelper.loadSubscribeConversationEntities(rssId);
        if (conversationEntities == null || conversationEntities.size() == 0) {
            SubscribeConversationEntity conversationEntity = new SubscribeConversationEntity();
            conversationEntity.setRssId(rssId);
            if (!TextUtils.isEmpty(title)) {
                conversationEntity.setTitle(title);
            }
            if (!TextUtils.isEmpty(icon)) {
                conversationEntity.setIcon(icon);
            }
            conversationEntity.setContent(showText);
            conversationEntity.setTime(msgtime);
            conversationEntity.setUnRead(1);
            SubscribeConversationHelper.subscribeConversationHelper.insertConversationEntity(conversationEntity);
        } else {
            SubscribeConversationHelper.subscribeConversationHelper.updataConversationEntity(rssId, showText, msgtime, 1);
        }
    }
}
