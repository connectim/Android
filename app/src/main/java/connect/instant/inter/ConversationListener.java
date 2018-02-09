package connect.instant.inter;

/**
 * Created by Administrator on 2017/10/19.
 */

public interface ConversationListener {

    void updateRoomMsg(String draft, String showText, long msgtime);

    void updateRoomMsg(String draft, String showText, long msgtime, int at);

    void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg);

    void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg, boolean broad);

    void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg, boolean broad,int attention);
}
