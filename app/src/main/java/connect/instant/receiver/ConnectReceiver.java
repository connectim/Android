package connect.instant.receiver;

import connect.database.green.DaoHelper.MessageHelper;
import connect.instant.model.CRobotChat;
import connect.utils.NotificationBar;
import instant.bean.ChatMsgEntity;
import connect.instant.bean.ConnectState;
import instant.bean.Session;
import instant.parser.inter.ConnectListener;
import instant.sender.model.RobotChat;
import instant.ui.InstantSdk;

/**
 * Created by Administrator on 2017/10/18.
 */

public class ConnectReceiver implements ConnectListener {

    private static String TAG = "_ConnectReceiver";

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
    public void welcome() {
        String mypublickey = Session.getInstance().getConnectCookie().getPubKey();
        ChatMsgEntity msgEntity = RobotChat.getInstance().txtMsg(InstantSdk.instantSdk.getBaseContext().getString(instant.R.string.Login_Welcome));
        msgEntity.setMessage_from(RobotChat.getInstance().nickName());
        msgEntity.setMessage_to(mypublickey);

        MessageHelper.getInstance().insertMsgExtEntity(msgEntity);
        CRobotChat.getInstance().updateRoomMsg(null, msgEntity.showContent(), msgEntity.getCreatetime(), -1, 1);
    }

    @Override
    public void notifyBarNotice(String pubkey, int type, String content) {
        NotificationBar.notificationBar.noticeBarMsg(pubkey,type,content);
    }
}
