package instant.bean;

import java.util.HashMap;
import java.util.Map;

import instant.utils.SharedUtil;
import instant.utils.StringUtil;

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
    public static String COOKIE_SHAKEHAND = "COOKIE_SHAKEHAND";

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
        return getUserCookie(CONNECT_USER);
    }

    /**
     * 缓存用户Cookie 随机的公私钥及SALT
     *
     * @return
     */
    public UserCookie getChatCookie() {
        return getUserCookie(COOKIE_USER);
    }

    /**
     * 握手时Cookie 加密StructData
     *
     * @return
     */
    public UserCookie getRandomCookie() {
        return getUserCookie(COOKIE_SHAKEHAND);
    }

    /**
     * 缓存好友Cookie 下发的好友pulicKey
     *
     * @param caPublicKey
     * @return
     */
    public UserCookie getFriendCookie(String caPublicKey) {
        return getUserCookie(caPublicKey);
    }

    public void setConnectCookie(UserCookie cookie) {
        setUserCookie(CONNECT_USER, cookie);
    }

    public void setChatCookie(UserCookie cookie) {
        setUserCookie(COOKIE_USER, cookie);
    }

    public void setRandomCookie(UserCookie cookie) {
        setUserCookie(COOKIE_SHAKEHAND, cookie);
    }


    public void setFriendCookie(String friendCaPublic, UserCookie cookie) {
        setUserCookie(friendCaPublic, cookie);
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

    private void setUserCookie(String pbk, UserCookie cookie) {
        if (userCookieMap == null) {
            userCookieMap = new HashMap<>();
        }
        userCookieMap.put(pbk, cookie);
    }

    private UserCookie getUserCookie(String pbk) {
        if (userCookieMap == null) {
            userCookieMap = new HashMap<>();
        }
        return userCookieMap.get(pbk);
    }

    public void removeCookie(String publicKey){
        if (userCookieMap == null) {
            userCookieMap = new HashMap<>();
        }
        userCookieMap.remove(publicKey);
    }

    public void removeConnectCookie(){
        removeCookie(CONNECT_USER);
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