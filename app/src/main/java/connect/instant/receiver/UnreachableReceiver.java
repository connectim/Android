package connect.instant.receiver;

import connect.utils.log.LogManager;
import instant.bean.Session;
import instant.bean.UserCookie;
import instant.parser.inter.UnreachableListener;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/10.
 */
public class UnreachableReceiver implements UnreachableListener {

    private String Tag = "_UnreachableReceiver";

    public static UnreachableReceiver receiver = getInstance();

    private synchronized static UnreachableReceiver getInstance() {
        if (receiver == null) {
            receiver = new UnreachableReceiver();
        }
        return receiver;
    }

    @Override
    public void notFriendNotice(String publicKey) {

    }

    @Override
    public void blackFriendNotice(String publicKey) {

    }

    @Override
    public void notGroupMemberNotice(String groupKey) {

    }

    @Override
    public void saltNotMatch(String msgid, String publicKey, Connect.ChatCookie cookie) throws Exception {
        String friendKey = publicKey;
        String cookiePubKey = "COOKIE:" + friendKey;
        Connect.ChatCookieData cookieData = cookie.getData();

        LogManager.getLogger().d(Tag, "saltNotMatch :" + msgid);
        UserCookie userCookie = new UserCookie();
        userCookie.setPubKey(cookieData.getChatPubKey());
        userCookie.setSalt(cookieData.getSalt().toByteArray());
        userCookie.setExpiredTime(cookieData.getExpired());
        Session.getInstance().setUserCookie(publicKey, userCookie);
    }

    @Override
    public void halfRandom(String msgid, String publicKey) throws Exception {

    }

    @Override
    public void receiveFailMsgs(String publicKey) {

    }
}
