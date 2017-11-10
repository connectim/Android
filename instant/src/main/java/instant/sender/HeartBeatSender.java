package instant.sender;

import com.google.protobuf.ByteString;

import instant.bean.Session;
import instant.bean.SocketACK;
import instant.bean.UserCookie;
import instant.parser.CommandParser;
import instant.utils.TimeUtil;

/**
 * Created by Administrator on 2017/9/30.
 */

public class HeartBeatSender {

    public void heartBeat(){
        SenderManager.getInstance().sendToMsg(SocketACK.HEART_BREAK, ByteString.copyFrom(new byte[0]));
        checkUserCookie();
    }

    /**
     * check cookie expire time
     */
    public void checkUserCookie() {
        boolean checkExpire = false;
        UserCookie userCookie = Session.getInstance().getChatCookie();
        if (userCookie != null) {
            long curTime = TimeUtil.getCurrentTimeSecond();
            checkExpire = curTime >= userCookie.getExpiredTime();
        }
        if (checkExpire) {
            try {
                CommandParser commandBean = new CommandParser((byte) 0x00, null);
                commandBean.chatCookieInfo(3);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
