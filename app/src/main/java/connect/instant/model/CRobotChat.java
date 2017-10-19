package connect.instant.model;

import connect.instant.inter.ConversationListener;
import instant.sender.model.RobotChat;

/**
 * Created by Administrator on 2017/10/19.
 */

public class CRobotChat extends RobotChat implements ConversationListener{

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
