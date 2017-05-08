package connect.ui.activity.chat.model;

import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.model.content.BaseChat;
import connect.ui.activity.chat.model.content.FriendChat;
import connect.ui.activity.chat.model.content.GroupChat;
import connect.ui.base.BaseApplication;

/**
 * Provide interface to access chat function to chat
 * Created by pujin on 2017/2/21.
 */
public class ChatOuterHelper {

    /**
     * friend/group send card
     *
     * @param type
     * @param objects
     */
    public static void sendCardTo(int type, Object... objects) {
        ContactEntity sendFriend = null;
        MsgEntity msgEntity = null;
        BaseChat baseChat = null;

        sendFriend = (ContactEntity) objects[0];
        switch (type) {
            case 0://friend
                ContactEntity receiveFriend = (ContactEntity) objects[1];
                baseChat = new FriendChat(receiveFriend);
                break;
            case 1://group
                GroupEntity receiveGroup = (GroupEntity) objects[1];
                baseChat = new GroupChat(receiveGroup);
                break;
        }

        msgEntity = (MsgEntity) baseChat.cardMsg(sendFriend);
        baseChat.sendPushMsg(msgEntity);
        MessageHelper.getInstance().insertToMsg(msgEntity.getMsgDefinBean());
        baseChat.updateRoomMsg(null, BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Visting_card), msgEntity.getMsgDefinBean().getSendtime());
    }
}
