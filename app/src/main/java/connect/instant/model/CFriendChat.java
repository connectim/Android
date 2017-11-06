package connect.instant.model;

import android.text.TextUtils;

import connect.activity.home.bean.ConversationAction;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ConversionEntity;
import connect.instant.inter.ConversationListener;
import instant.sender.model.FriendChat;

/**
 * Created by Administrator on 2017/10/19.
 */

public class CFriendChat extends FriendChat implements ConversationListener{

    private ContactEntity contactEntity;

    public CFriendChat(String friendKey) {
        super(friendKey);

        ContactEntity contactEntity = ContactHelper.getInstance().loadFriendEntity(friendKey);
        this.contactEntity = contactEntity;
        this.friendKey = contactEntity.getUid();
    }

    public CFriendChat(ContactEntity contactEntity) {
        super(contactEntity.getUid());
        this.contactEntity = contactEntity;
        this.friendKey = contactEntity.getUid();
    }

    @Override
    public String headImg() {
        super.headImg();
        return contactEntity.getAvatar();
    }

    @Override
    public String nickName() {
        super.nickName();
        String nickName = TextUtils.isEmpty(contactEntity.getRemark()) ? contactEntity.getUsername() : contactEntity.getRemark();
        return nickName;
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

        ConversionEntity conversionEntity = ConversionHelper.getInstance().loadRoomEnitity(chatKey());
        if (conversionEntity == null) {
            conversionEntity = new ConversionEntity();
            conversionEntity.setIdentifier(contactEntity.getUid());
            conversionEntity.setName(nickName());
            conversionEntity.setAvatar(headImg());
        }

        conversionEntity.setType(chatType());
        if (!TextUtils.isEmpty(showText)) {
            conversionEntity.setContent(showText);
        }
        if (msgtime > 0) {
            conversionEntity.setLast_time(msgtime);
        }
        conversionEntity.setStranger(isStranger ? 1 : 0);

        if (newmsg == 0) {
            conversionEntity.setUnread_count(0);
        } else if (newmsg > 0) {
            int unread = (null == conversionEntity.getUnread_count()) ? 1 : 1 + conversionEntity.getUnread_count();
            conversionEntity.setUnread_count(unread);
        }

        if (draft != null) {
            conversionEntity.setDraft(draft);
        }
        if (at >= 0) {
            conversionEntity.setNotice(at);
        }

        ConversionHelper.getInstance().insertRoomEntity(conversionEntity);
        if (broad) {
            ConversationAction.conversationAction.sendEvent();
        }
    }
}
