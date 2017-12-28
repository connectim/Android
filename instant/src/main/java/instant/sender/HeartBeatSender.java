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

    public void heartBeat() {
        SenderManager.getInstance().sendToMsg(SocketACK.HEART_BREAK, ByteString.copyFrom(new byte[0]));
    }
}
