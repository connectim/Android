package connect.instant.receiver;

import connect.utils.NotificationBar;
import instant.bean.ChatMsgEntity;
import instant.bean.ConnectState;
import instant.parser.inter.ConnectListener;

/**
 * Created by Administrator on 2017/10/18.
 */

public class ConnectReceiver implements ConnectListener{

    private String Tag = "_ConnectReceiver";

    public static ConnectReceiver receiver = getInstance();

    private synchronized static ConnectReceiver getInstance() {
        if (receiver == null) {
            receiver = new ConnectReceiver();
        }
        return receiver;
    }

    @Override
    public void disConnect() {
        ConnectState.getInstance().sendEvent(ConnectState.ConnectType.DISCONN);
    }

    @Override
    public void requestLogin() {
        ConnectState.getInstance().sendEvent(ConnectState.ConnectType.REFRESH_ING);
    }

    @Override
    public void loginSuccess() {
        ConnectState.getInstance().sendEvent(ConnectState.ConnectType.REFRESH_SUCCESS);
    }

    @Override
    public void pullOfflineMessage() {
        ConnectState.getInstance().sendEvent(ConnectState.ConnectType.OFFLINE_PULL);
    }

    @Override
    public void connectSuccess() {
        ConnectState.getInstance().sendEvent(ConnectState.ConnectType.CONNECT);
    }

    @Override
    public void welcome(ChatMsgEntity chatMsgEntity) {

//        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
//        RobotChat.getInstance().updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, 1);

    }

    @Override
    public void notifyBarNotice(String pubkey, int type, String content) {
        NotificationBar.notificationBar.noticeBarMsg(pubkey,type,content);
    }
}
