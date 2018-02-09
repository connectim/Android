package instant.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import instant.bean.UserCookie;
import instant.ui.InstantSdk;

/**
 * Created by Administrator on 2017/10/18.
 */

public class SharedUtil {

    /**
     * user SharedPreference name
     */
    private static final String SHAREPREFERENCES_DEFAULT = "SHAREPREFERENCES_DEFAULT";
    private static final String SHAREPREFERENCES_NAME = "SHARE_INSTANT";
    private static SharedUtil sharePreUtil;
    private static SharedPreferences sharePre;

    private static String TAG = "_SharedUtil";

    public static String CONTACTS_VERSION = "CONTACTS_VERSION";
    public static String WELCOME_VERSION = "WELCOME_VERSION";
    public static String UPLOAD_APPINFO_VERSION = "UPLOAD_APPINFO_VERSION";
    public static String COOKIE_CONNECT_USER = "COOKIE_CONNECT_USER";
    public static String COOKIE_CHATUSER = "COOKIE_CHATUSER";

    public static SharedUtil getInstance() {
        return getInstance(InstantSdk.getInstance().getBaseContext());
    }

    private synchronized static SharedUtil getInstance(Context context) {
        if (null == sharePreUtil || null == sharePre) {
            sharePreUtil = new SharedUtil();
            UserCookie userCookie = InstantSdk.getInstance().getDefaultCookie();
            if (null == userCookie || TextUtils.isEmpty(userCookie.getUid())) {
                userCookie = sharePreUtil.loadDefaultConnectCookie();
            }
            String myUid = userCookie.getUid();
            sharePre = context.getSharedPreferences(SHAREPREFERENCES_NAME + ":" + myUid, Context.MODE_PRIVATE);
        }
        return sharePreUtil;
    }

    /****************************** Default Cookie ********************************************************/
    public void insertDefaultConnectCookie(UserCookie userCookie) {
        Context context = InstantSdk.getInstance().getBaseContext();
        SharedPreferences defaultSharePre = context.getSharedPreferences(SHAREPREFERENCES_DEFAULT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = defaultSharePre.edit();
        editor.putString(COOKIE_CONNECT_USER, new Gson().toJson(userCookie));
        editor.apply();
    }

    public UserCookie loadDefaultConnectCookie() {
        Context context = InstantSdk.getInstance().getBaseContext();
        SharedPreferences defaultSharePre = context.getSharedPreferences(SHAREPREFERENCES_DEFAULT, Context.MODE_PRIVATE);
        String connectCookieKey = COOKIE_CONNECT_USER;
        UserCookie userCookie = null;
        if (defaultSharePre.contains(connectCookieKey)) {
            String gsonCookie = defaultSharePre.getString(connectCookieKey, "");
            if (!TextUtils.isEmpty(gsonCookie)) {
                userCookie = new Gson().fromJson(gsonCookie, UserCookie.class);
            }
        }
        return userCookie;
    }

    public void closeShare() {
        if (sharePre != null) {
            sharePre = null;
        }
        if (sharePreUtil != null) {
            sharePreUtil = null;
        }
    }

    public void putValue(String key, String value) {
        if (sharePre == null) {
            getInstance();
        }
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void putValue(String key, int value) {
        if (sharePre == null) {
            getInstance();
        }
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public String getStringValue(String key) {
        if (sharePre == null) {
            getInstance();
        }
        return sharePre.getString(key, "");
    }

    public int getIntValue(String key) {
        if (sharePre == null) {
            getInstance();
        }
        return sharePre.getInt(key, 0);
    }

    public boolean isContains(String key) {
        if (sharePre == null) {
            getInstance();
        }
        return sharePre.contains(key);
    }

    public void remove(String key) {
        if (sharePre == null) {
            getInstance();
        }
        SharedPreferences.Editor editor = sharePre.edit();
        editor.remove(key);
        editor.apply();
    }

    public void deleteUserInfo() {
        if (sharePre == null) {
            getInstance();
        }

        sharePre.edit()
                .clear()
                .apply();
        sharePre = null;
    }

    /******************************  Connect Cookie  ********************************************************/
    public void insertConnectCookie(UserCookie userCookie) {
        String connectCookieKey = COOKIE_CONNECT_USER;

        putValue(connectCookieKey, new Gson().toJson(userCookie));
        insertDefaultConnectCookie(userCookie);
    }

    public UserCookie loadConnectCookie() {
        String connectCookieKey = COOKIE_CONNECT_USER;
        UserCookie userCookie = null;
        if (sharePre == null) {
            getInstance();
        }
        if (sharePre.contains(connectCookieKey)) {
            String gsonCookie = sharePre.getString(connectCookieKey, "");
            if (!TextUtils.isEmpty(gsonCookie)) {
                userCookie = new Gson().fromJson(gsonCookie, UserCookie.class);
            }
        }
        return userCookie;
    }

    /******************************  Chat Cookie  ********************************************************/
    public void insertChatUserCookie(UserCookie userCookie) {
        if (sharePre == null) {
            getInstance();
        }
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
        if (sharePre == null) {
            getInstance();
        }
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
}
