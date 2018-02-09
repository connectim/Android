package connect.instant.model;

import android.text.TextUtils;

import java.util.List;

import connect.activity.home.bean.ConversationAction;
import connect.activity.home.bean.RoomAttrBean;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.bean.ConversionEntity;
import connect.database.green.bean.GroupEntity;
import connect.instant.inter.ConversationListener;
import connect.utils.RegularUtil;
import instant.sender.model.GroupChat;

/**
 * Created by Administrator on 2017/10/19.
 */
public class CGroupChat extends GroupChat implements ConversationListener {

    private GroupEntity groupEntity;

    public CGroupChat(String groupKey) {
        super(groupKey);
        this.groupKey = groupKey;

        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
        this.groupEntity = groupEntity;
        this.groupKey = groupEntity.getIdentifier();
        this.groupName = groupEntity.getName();
    }

    public CGroupChat(GroupEntity groupEntity) {
        super(groupEntity.getIdentifier());
        this.groupEntity = groupEntity;
        this.groupKey = groupEntity.getIdentifier();
        this.groupName = groupEntity.getName();
    }

    @Override
    public String headImg() {
        String groupAvatar = groupEntity.getAvatar();
        if (TextUtils.isEmpty(groupAvatar)) {
            groupAvatar = RegularUtil.groupAvatar(groupKey);
        }
        return groupAvatar;
    }

    @Override
    public String nickName() {
        return groupEntity.getName();
    }

    public void updateRoomMsg(String draft, String showText, long msgtime) {
        updateRoomMsg(draft, showText, msgtime, 0);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at) {
        updateRoomMsg(draft, showText, msgtime, at, 0);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg) {
        updateRoomMsg(draft, showText, msgtime, at, newmsg, true);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg, boolean broad) {
        updateRoomMsg(draft, showText, msgtime, at, newmsg, true, 0);
    }

    @Override
    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg, boolean broad, int attention) {
        if (TextUtils.isEmpty(chatKey())) {
            return;
        }

        List<RoomAttrBean> roomEntities = ConversionHelper.getInstance().loadRoomEntities(chatKey());
        if (roomEntities == null || roomEntities.size() == 0) {
            int atCount = at;
            if (at == -1) {
                atCount = 0;
            }

            int unreadCount = newmsg;
            if (newmsg == -1) {
                unreadCount = 0;
            }

            int attentionCount = attention;
            if (attention == -1) {
                attentionCount = 0;
            }

            ConversionEntity conversionEntity = new ConversionEntity();
            conversionEntity.setIdentifier(groupKey);
            conversionEntity.setName(nickName());
            conversionEntity.setAvatar(headImg());
            conversionEntity.setType(chatType());
            conversionEntity.setContent(TextUtils.isEmpty(showText) ? "" : showText);
            conversionEntity.setUnread_count(unreadCount);
            conversionEntity.setDraft(TextUtils.isEmpty(draft) ? "" : draft);
            conversionEntity.setUnread_at(atCount);
            conversionEntity.setUnread_attention(attentionCount);
            conversionEntity.setLast_time((msgtime < 0 ? 0 : msgtime));
            ConversionHelper.getInstance().insertRoomEntity(conversionEntity);
        } else {
            for (RoomAttrBean attrBean : roomEntities) {
                int atCount = attrBean.getUnreadAt();
                if (at == 0) {
                    atCount = 0;
                } else if (at == 1) {
                    atCount = 1 + attrBean.getUnreadAt();
                } else if (at == -1) {
                    atCount = attrBean.getUnreadAt();
                }

                int unreadCount = newmsg;
                if (newmsg == -1) {
                    unreadCount = attrBean.getUnread();
                } else if (newmsg == 0) {
                    unreadCount = 0;
                } else {
                    unreadCount = 1 + attrBean.getUnread();
                }

                int attentionCount = attrBean.getUnreadAttention();
                if (attention == 0) {
                    attentionCount = 0;
                } else if (attention == 1) {
                    attentionCount = 1 + attrBean.getUnreadAttention();
                } else if (attention == -1) {
                    attentionCount = attrBean.getUnreadAttention();
                }

                ConversionHelper.getInstance().updateRoomEntity(
                        groupKey,
                        nickName(),
                        headImg(),
                        TextUtils.isEmpty(draft) ? "" : draft,
                        TextUtils.isEmpty(showText) ? "" : showText,
                        (unreadCount),
                        atCount,
                        (msgtime < 0 ? 0 : msgtime),
                        attentionCount
                );
            }
        }
        if (broad) {
            ConversationAction.conversationAction.sendEvent(ConversationAction.ConverType.LOAD_MESSAGE);
        }
    }
}
