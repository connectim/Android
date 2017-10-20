package instant.parser.localreceiver;

import instant.bean.ChatMsgEntity;
import instant.parser.inter.ConnectListener;

/**
 * Created by Administrator on 2017/10/18.
 */

public class ConnectLocalReceiver implements ConnectListener{

    public static ConnectLocalReceiver receiver = getInstance();

    private synchronized static ConnectLocalReceiver getInstance() {
        if (receiver == null) {
            receiver = new ConnectLocalReceiver();
        }
        return receiver;
    }

    private ConnectListener connectListener = null;

    public void registerConnect(ConnectListener listener) {
        this.connectListener = listener;
    }

    public ConnectListener getConnectListener() {
        if (connectListener == null) {
            throw new RuntimeException("commandListener don't register");
        }
        return connectListener;
    }

    @Override
    public void disConnect() {
        getConnectListener().disConnect();
    }

    @Override
    public void requestLogin() {
        getConnectListener().requestLogin();
    }

    @Override
    public void loginSuccess() {
        getConnectListener().loginSuccess();
    }

    @Override
    public void pullOfflineMessage() {
        getConnectListener().pullOfflineMessage();
    }

    @Override
    public void connectSuccess() {
        getConnectListener().connectSuccess();
    }

    @Override
    public void welcome(ChatMsgEntity chatMsgEntity) {
        getConnectListener().welcome(chatMsgEntity);
    }

    @Override
    public void notifyBarNotice(String pubkey, int type, String content) {
        getConnectListener().notifyBarNotice(pubkey, type, content);
    }
}
