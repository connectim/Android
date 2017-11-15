package instant.parser.localreceiver;

import instant.parser.inter.UnreachableListener;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/9.
 */
public class UnreachableLocalReceiver implements UnreachableListener {

    public static UnreachableLocalReceiver localReceiver = getInstance();

    private synchronized static UnreachableLocalReceiver getInstance() {
        if (localReceiver == null) {
            localReceiver = new UnreachableLocalReceiver();
        }
        return localReceiver;
    }

    private UnreachableListener unreachableListener;

    public void registerUnreachableListener(UnreachableListener listener) {
        this.unreachableListener = listener;
    }

    public UnreachableListener getUnreachableListener() {
        if (unreachableListener == null) {
            throw new RuntimeException("unreachableListener don't registe");
        }
        return unreachableListener;
    }

    @Override
    public void notFriendNotice(String publicKey) {
        getUnreachableListener().notFriendNotice(publicKey);
    }

    @Override
    public void blackFriendNotice(String publicKey) {
        getUnreachableListener().blackFriendNotice(publicKey);
    }

    @Override
    public void notGroupMemberNotice(String groupKey) {
        getUnreachableListener().notGroupMemberNotice(groupKey);
    }

    @Override
    public void friendCookieExpired(String rejectUid) {
        getUnreachableListener().friendCookieExpired(rejectUid);
    }

    @Override
    public void saltNotMatch(String msgid, String rejectUid, Connect.ChatCookie cookie) throws Exception {
        getUnreachableListener().saltNotMatch(msgid, rejectUid, cookie);
    }
}
