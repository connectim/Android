package connect.instant.inter;

/**
 * Created by Administrator on 2017/10/19.
 */

public interface ConversationListener {

    public void updateRoomMsg(String draft, String showText, long msgtime) ;

    public void updateRoomMsg(String draft, String showText, long msgtime, int at) ;

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg) ;

    public void updateRoomMsg(String draft, String showText, long msgtime, int at, int newmsg, boolean broad);
}
