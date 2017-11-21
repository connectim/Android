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
public class CGroupChat extends GroupChat implements ConversationListener{

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
        return RegularUtil.groupAvatar(groupKey);
    }

    @Override
    public String nickName() {
        return groupEntity.getName();
    }

    public void updateRoomMsg(String draft, String showText, long msgtime) {
        updateRoomMsg(draft, showText, msgtime, -1);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at) {
        updateRoomMsg(draft, showText, msgtime, at, 0);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg) {
        updateRoomMsg(draft,showText,msgtime,at,newmsg,true);
    }

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg, boolean broad) {
        if (TextUtils.isEmpty(chatKey())) {
            return;
        }

        List<RoomAttrBean> roomEntities = ConversionHelper.getInstance().loadRoomEntities(chatKey());
        if (roomEntities == null || roomEntities.size() == 0) {
            ConversionEntity conversionEntity = new ConversionEntity();
            conversionEntity.setIdentifier(groupKey);
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
                        groupKey,
                        TextUtils.isEmpty(draft) ? "" : draft,
                        TextUtils.isEmpty(showText) ? "" : showText,
                        (newmsg == 0 ? 0 : 1 + attrBean.getUnread()),
                        at,
                        (isStranger ? 1 : 0),
                        (msgtime > 0 ? 0 : msgtime)
                );
            }
        }
        if (broad) {
            ConversationAction.conversationAction.sendEvent(ConversationAction.ConverType.LOAD_MESSAGE);
        }
    }
}
