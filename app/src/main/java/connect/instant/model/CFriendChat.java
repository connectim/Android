package connect.instant.model;

import connect.database.green.bean.ContactEntity;
import connect.instant.inter.ConversationListener;
import instant.sender.model.FriendChat;

/**
 * Created by Administrator on 2017/10/19.
 */

public class CFriendChat extends FriendChat implements ConversationListener{

    public CFriendChat(ContactEntity contactEntity) {
        super(contactEntity.getPub_key());
        this.friendKey = contactEntity.getPub_key();
    }

    @Override
    public void updateRoomMsg(String draft, String showText, long msgtime) {

    }

    @Override
    public void updateRoomMsg(String draft, String showText, long msgtime, int at) {

    }

    @Override
    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg) {

    }

    @Override
    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg, boolean broad) {

    }
}
