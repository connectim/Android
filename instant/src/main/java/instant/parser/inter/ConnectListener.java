package instant.parser.inter;

import instant.bean.ChatMsgEntity;

/**
 * Created by Administrator on 2017/10/18.
 */
public interface ConnectListener {

    void disConnect();

    void requestLogin();

    void loginSuccess();

    void pullOfflineMessage();

    void connectSuccess();

    void welcome(ChatMsgEntity chatMsgEntity);

    void notifyBarNotice(String pubkey, int type, String content);
}
