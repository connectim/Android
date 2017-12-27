package instant.bean;

import java.util.HashMap;
import java.util.Map;

import instant.utils.SharedUtil;

/**
 * Session
 * Created by gtq on 2016/11/30.
 */
public class Session {

    private static String TAG = "Session";
    private static Session session;

    public synchronized static Session getInstance() {
        if (session == null) {
            session = new Session();
        }
        return session;
    }

    /** Connected user related information */
    public static String CONNECT_USER = "CONNECT_USER";
    public static String COOKIE_USER = "COOKIE_USER";

    /** Cookie upload fail number */
    private Map<String, Integer> cookieUpTimer = new HashMap<>();
    /** user cookie bean */
    private Map<String, UserCookie> userCookieMap = new HashMap<>();

    /****************************************  LOAD COOKIE    ***************************************/

    /**
     * 缓存用户信息 CA公私钥及UID
     *
     * @return
     */
    public UserCookie getConnectCookie() {
        UserCookie userCookie = getUserCookie(CONNECT_USER);
        if (userCookie == null) {
            userCookie = SharedUtil.getInstance().loadConnectCookie();
            setConnectCookie(userCookie);
        }
        return userCookie;
    }

    /**
     * 缓存用户Cookie 随机的公私钥及SALT
     *
     * @return
     */
    public UserCookie getChatCookie() {
        UserCookie userCookie = getUserCookie(COOKIE_USER);
        if (userCookie == null) {
            userCookie = SharedUtil.getInstance().loadLastChatUserCookie();
            setChatCookie(userCookie);
        }
        return userCookie;
    }


    public void setConnectCookie(UserCookie cookie) {
        setUserCookie(CONNECT_USER, cookie);
        SharedUtil.getInstance().insertConnectCookie(cookie);
    }

    public void setChatCookie(UserCookie cookie) {
        setUserCookie(COOKIE_USER, cookie);
        SharedUtil.getInstance().insertChatUserCookie(cookie);
    }

    private void setUserCookie(String pbk, UserCookie cookie) {
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

    public void removeCookie(String publicKey) {
        if (userCookieMap == null) {
            userCookieMap = new HashMap<>();
        }
        userCookieMap.remove(publicKey);
    }

    /****************************************  CONNECT FAIL TIME    ***************************************/
    public int getUpFailTime(String pbk) {
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

    public void clearUserCookie() {
        if (userCookieMap != null) {
            userCookieMap.clear();
        }
        if (cookieUpTimer != null) {
            cookieUpTimer.clear();
        }
    }
}