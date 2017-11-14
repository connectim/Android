package instant.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import instant.bean.Session;
import instant.bean.UserCookie;
import instant.ui.InstantSdk;

/**
 * Created by Administrator on 2017/10/18.
 */

public class SharedUtil {

    /** user SharedPreference name */
    private static final String SHAREPREFERENCES_NAME = "SHARE_INSTANT";
    private static SharedUtil sharePreUtil;
    private static SharedPreferences sharePre;

    private static String TAG = "_SharedUtil";

    public static String CONTACTS_VERSION = "CONTACTS_VERSION";
    public static String WELCOME_VERSION = "WELCOME_VERSION";
    public static String UPLOAD_APPINFO_VERSION = "UPLOAD_APPINFO_VERSION";
    public static String COOKIE_CHATUSER = "COOKIE_CHATUSER";
    public static String COOKIE_CHATFRIEND = "COOKIE_CHATFRIEND:";

    public static SharedUtil getInstance() {
        return getInstance(InstantSdk.instantSdk.getBaseContext());
    }

    private synchronized static SharedUtil getInstance(Context context) {
        if (null == sharePreUtil || null == sharePre) {
            sharePreUtil = new SharedUtil();
            String myUid = Session.getInstance().getConnectCookie().getUid();
            sharePre = context.getSharedPreferences(SHAREPREFERENCES_NAME + ":" + myUid, Context.MODE_PRIVATE);
        }
        return sharePreUtil;
    }

    public void closeShare() {
        if (sharePre != null) {
            sharePre = null;
        }
        if (sharePreUtil != null) {
            sharePreUtil = null;
        }
    }

    public void putValue(String key,String value) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void putValue(String key, int value) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public String getStringValue(String key) {
        return sharePre.getString(key, "");
    }

    public int getIntValue(String key) {
        return sharePre.getInt(key, 0);
    }

    public boolean isContains(String key) {
        return sharePre.contains(key);
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.remove(key);
        editor.apply();
    }

    public void deleteUserInfo(){
        sharePre.edit()
                .clear()
                .commit();
    }

    /******************************  User Cookie  ********************************************************/
    public void insertChatUserCookie(UserCookie userCookie) {
        if (sharePre.contains(COOKIE_CHATUSER)) {
            String gsonCookies = sharePre.getString(COOKIE_CHATUSER, "");
            if (TextUtils.isEmpty(gsonCookies)) {
                remove(COOKIE_CHATUSER);
                insertChatUserCookie(userCookie);
            } else {
                List<UserCookie> userCookieList = new Gson().fromJson(gsonCookies, new TypeToken<List<UserCookie>>() {
                }.getType());
                if (userCookieList.size() >= 5) {
                    int cookiesLength = userCookieList.size();
                    userCookieList = userCookieList.subList(cookiesLength - 4, cookiesLength);
                }
                userCookieList.add(userCookie);
                putValue(COOKIE_CHATUSER, new Gson().toJson(userCookieList));
            }
        } else {
            List<UserCookie> userCookieList = new ArrayList<>();
            userCookieList.add(userCookie);
            putValue(COOKIE_CHATUSER, new Gson().toJson(userCookieList));
        }
    }

    public UserCookie loadLastChatUserCookie() {
        UserCookie userCookie = null;
        if (sharePre.contains(COOKIE_CHATUSER)) {
            String gsonCookies = sharePre.getString(COOKIE_CHATUSER, "");
            if (!TextUtils.isEmpty(gsonCookies)) {
                List<UserCookie> userCookieList = new Gson().fromJson(gsonCookies, new TypeToken<List<UserCookie>>() {
                }.getType());

                int cookiesLength = userCookieList.size();
                userCookie = userCookieList.get(cookiesLength - 1);
            }
        }
        return userCookie;
    }

    public UserCookie loadChatUserCookieBySalt(String saltHex) {
        UserCookie userCookie = null;
        if (sharePre.contains(COOKIE_CHATUSER)) {
            String gsonCookies = sharePre.getString(COOKIE_CHATUSER, "");
            if (!TextUtils.isEmpty(gsonCookies)) {
                List<UserCookie> userCookieList = new Gson().fromJson(gsonCookies, new TypeToken<List<UserCookie>>() {
                }.getType());

                for (UserCookie tempCookie : userCookieList) {
                    String tempHex = StringUtil.bytesToHexString(tempCookie.getSalt());
                    if (tempHex.equals(saltHex)) {
                        userCookie = tempCookie;
                        break;
                    }
                }
            }
        }
        return userCookie;
    }


    /******************************  Friend Cookie  ********************************************************/
    public void insertFriendCookie(String friendCaPublicKey, UserCookie userCookie) {
        String friendCookieKey = COOKIE_CHATFRIEND + friendCaPublicKey;
        if (sharePre.contains(friendCookieKey)) {
            String gsonCookies = sharePre.getString(friendCookieKey, "");
            if (TextUtils.isEmpty(gsonCookies)) {
                remove(friendCookieKey);
                insertFriendCookie(friendCaPublicKey, userCookie);
            } else {
                List<UserCookie> friendCookieList = new Gson().fromJson(gsonCookies, new TypeToken<List<UserCookie>>() {
                }.getType());
                if (friendCookieList.size() >= 3) {
                    int cookiesLength = friendCookieList.size();
                    friendCookieList = friendCookieList.subList(cookiesLength - 2, cookiesLength);
                }
                friendCookieList.add(userCookie);
                putValue(friendCookieKey, new Gson().toJson(friendCookieList));
            }
        } else {
            List<UserCookie> userCookieList = new ArrayList<>();
            userCookieList.add(userCookie);
            putValue(friendCookieKey, new Gson().toJson(userCookieList));
        }
    }

    public UserCookie loadFriendCookie(String friendCaPublicKey) {
        String friendCookieKey = COOKIE_CHATFRIEND + friendCaPublicKey;
        UserCookie userCookie = null;
        if (sharePre.contains(friendCookieKey)) {
            String gsonCookies = sharePre.getString(friendCookieKey, "");
            if (!TextUtils.isEmpty(gsonCookies)) {
                List<UserCookie> userCookieList = new Gson().fromJson(gsonCookies, new TypeToken<List<UserCookie>>() {
                }.getType());

                int cookiesLength = userCookieList.size();
                userCookie = userCookieList.get(cookiesLength-1);
            }
        }
        return userCookie;
    }
}
