package instant.parser.inter;

import protos.Connect;

/**
 * Created by Administrator on 2017/10/9.
 */

public interface MessageListener {

    void singleChat(Connect.ChatMessage chatMessages, String publicKey,byte[] contents) throws Exception;

    void groupChat(Connect.MessagePost messagePost);

    void inviteJoinGroup( Connect.CreateGroupMessage groupMessage);
}
