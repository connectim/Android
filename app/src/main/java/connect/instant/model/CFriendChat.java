package connect.instant.model;

import android.text.TextUtils;

import java.util.List;

import connect.activity.base.BaseApplication;
import connect.activity.home.bean.ConversationAction;
import connect.activity.home.bean.RoomAttrBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ConversionEntity;
import connect.instant.inter.ConversationListener;
import connect.ui.activity.R;
import connect.utils.TimeUtil;
import instant.bean.ChatMsgEntity;
import instant.sender.model.FriendChat;

/**
 * Created by Administrator on 2017/10/19.
 */

public class CFriendChat extends FriendChat implements ConversationListener{

    private ContactEntity contactEntity;

    public CFriendChat(String uid) {
        super(uid);
        ContactEntity contactEntity = ContactHelper.getInstance().loadFriendEntity(uid);
        this.contactEntity = contactEntity;
    }

    public CFriendChat(ContactEntity contactEntity) {
        super(contactEntity.getUid());
        this.contactEntity = contactEntity;
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

    /**
     * 打招呼 消息
     */
    public void createWelcomeMessage() {
        String myUid = SharedPreferenceUtil.getInstance().getUser().getUid();
        String content = BaseApplication.getInstance().getBaseContext().getString(R.string.Link_Hello_I_am, nickName());
        ChatMsgEntity msgExtEntity = txtMsg(content);
        msgExtEntity.setMessage_from(friendUid);
        msgExtEntity.setMessage_to(myUid);
        updateRoomMsg("", content, TimeUtil.getCurrentTimeInLong(), -1, 1);

        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
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
            conversionEntity.setIdentifier(friendUid);
            conversionEntity.setName(nickName());
            conversionEntity.setAvatar(headImg());
            conversionEntity.setType(chatType());
            conversionEntity.setContent(TextUtils.isEmpty(showText) ? "" : showText);
            conversionEntity.setStranger(isStranger ? 1 : 0);
            conversionEntity.setUnread_count(newmsg == 0 ? 0 : 1);
            conversionEntity.setDraft(TextUtils.isEmpty(draft) ? "" : draft);
            conversionEntity.setIsAt(at);
            conversionEntity.setLast_time((msgtime > 0 ? 0 : msgtime));
            ConversionHelper.getInstance().insertRoomEntity(conversionEntity);
        } else {
            for (RoomAttrBean attrBean : roomEntities) {
                ConversionHelper.getInstance().updateRoomEntity(
                        friendUid,
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
