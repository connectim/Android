package instant.parser.inter;

import protos.Connect;

/**
 * Created by Administrator on 2017/10/9.
 */

public interface UnreachableListener {

    void notFriendNotice(String publicKey);

    void blackFriendNotice(String publicKey);

    void notGroupMemberNotice(String groupKey);

    void friendCookieExpired(String rejectUid);

    void saltNotMatch(String msgid, String rejectUid, Connect.ChatCookie cookie) throws Exception;
}
