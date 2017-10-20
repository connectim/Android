package instant.bean;

import java.util.HashMap;
import java.util.Map;

import instant.parser.localreceiver.CommandLocalReceiver;
import instant.utils.SharedUtil;
import instant.utils.StringUtil;

/**
 * Session
 * Created by gtq on 2016/11/30.
 */
public class Session {

    private static String Tag = "Session";
    private static Session session;

    public static Session getInstance() {
        if (session == null) {
            synchronized (Session.class) {
                if (session == null) {
                    session = new Session();
                }
            }
        }
        return session;
    }

    /** Connected user related information */
    public static String CONNECT_USER = "CONNECT_USER";
    public static String COOKIE_SHAKEHAND = "COOKIE_SHAKEHAND";

    /** Cookie upload fail number */
    private Map<String, Integer> cookieUpTimer = new HashMap<>();
    /** user cookie bean */
    private Map<String, UserCookie> userCookieMap = new HashMap<>();


    public synchronized int getUpFailTime(String pbk) {
        if (cookieUpTimer == null) {
            cookieUpTimer = new HashMap<>();
        }

        Integer value = cookieUpTimer.get(pbk);
        return value == null ? 0 : value;
    }

    public void setUpFailTime(String pbk, int time) {
        if (cookieUpTimer == null) {
            cookieUpTimer = new HashMap<>();
        }
        cookieUpTimer.put(pbk, time);
    }

    public void setUserCookie(String pbk, UserCookie cookie) {
        if (userCookieMap == null) {
            userCookieMap = new HashMap<>();
        }
        userCookieMap.put(pbk, cookie);
    }

    public UserCookie getUserCookie(String pbk) {
        if (userCookieMap == null) {
            userCookieMap = new HashMap<>();
        }
        return userCookieMap.get(pbk);
    }

    public UserCookie getCookieBySalt(String saltHex) {
        String pubkey = getUserCookie(Session.CONNECT_USER).getPubKey();
        UserCookie userCookie = getUserCookie(pubkey);
        if (userCookie != null) {
            String userHex = StringUtil.bytesToHexString(userCookie.getSalt());
            if (userHex.equals(saltHex)) {

            } else {
                userCookie = null;
            }
        }

        if (userCookie == null) {
            userCookie = SharedUtil.getInstance().loadChatUserCookieBySalt(saltHex);
        }
        return userCookie;
    }

    public void clearUserCookie() {
        if (userCookieMap == null) {
            userCookieMap = new HashMap<>();
        }
        userCookieMap.clear();
    }
}