package instant.parser.inter;

import protos.Connect;

/**
 * Created by Administrator on 2017/10/9.
 */

public interface MessageListener {

    long chatBurnTime(String publicKey);

    void singleChat(Connect.MessageData messageData) throws Exception;

    void groupChat(Connect.ChatMessage messagePost);

    void inviteJoinGroup(Connect.CreateGroupMessage groupMessage);
}
