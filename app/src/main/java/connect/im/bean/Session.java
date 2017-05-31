package connect.im.bean;

import java.util.HashMap;
import java.util.Map;

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

    public void clearUserCookie() {
        if (userCookieMap == null) {
            userCookieMap = new HashMap<>();
        }
        userCookieMap.clear();
    }
}