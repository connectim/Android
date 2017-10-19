package connect.instant.model;

import connect.database.green.bean.GroupEntity;
import connect.instant.inter.ConversationListener;
import instant.sender.model.GroupChat;

/**
 * Created by Administrator on 2017/10/19.
 */

public class CGroupChat extends GroupChat implements ConversationListener{

    public CGroupChat(GroupEntity groupEntity) {
        super(groupEntity.getIdentifier());
        this.groupKey = groupEntity.getIdentifier();
        this.groupEcdh = groupEntity.getEcdh_key();
        this.groupName = groupEntity.getName();
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
