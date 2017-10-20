package instant.utils;

import android.text.TextUtils;

import instant.R;
import instant.ui.InstantSdk;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/20.
 */

public class ChatUtil {

    public static Connect.ChatType parseChatType(String publicKey) {
        Connect.ChatType chatType = null;
        if (publicKey.equals(InstantSdk.instantSdk.getBaseContext().getString(R.string.Connect_name))) {
            chatType = Connect.ChatType.CONNECT_SYSTEM;
        } else if (TextUtils.isEmpty(publicKey)) {
            chatType = Connect.ChatType.PRIVATE;
        } else if (true) {
            chatType = Connect.ChatType.GROUPCHAT;
        }

        return chatType;
    }
}
