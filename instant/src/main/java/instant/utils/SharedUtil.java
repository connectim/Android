package instant.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import instant.bean.Session;
import instant.bean.UserCookie;
import instant.ui.InstantSdk;

/**
 * Created by Administrator on 2017/10/18.
 */

public class SharedUtil {

    /** user SharedPreference name */
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
    public static String COOKIE_RANDOM = "COOKIE_RANDOM";
    public static String COOKIE_CHATFRIEND = "COOKIE_CHATFRIEND:";
    public static String COOKIE_CHATGROUP_MEMBER = "COOKIE_CHATGROUP_MEMBER";

    public static SharedUtil getInstance() {
        return getInstance(InstantSdk.instantSdk.getBaseContext());
    }

    private synchronized static SharedUtil getInstance(Context context) {
        if (null == sharePreUtil || null == sharePre) {
            sharePreUtil = new SharedUtil();
            UserCookie userCookie = Session.getInstance().getUserCookie(Session.CONNECT_USER);
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
        Context context = InstantSdk.instantSdk.getBaseContext();
        SharedPreferences defaultSharePre = context.getSharedPreferences(SHAREPREFERENCES_DEFAULT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = defaultSharePre.edit();
        editor.putString(COOKIE_CONNECT_USER, new Gson().toJson(userCookie));
        editor.apply();
    }

    public UserCookie loadDefaultConnectCookie() {
        Context context = InstantSdk.instantSdk.getBaseContext();
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
                .commit();
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

    public UserCookie loadChatUserCookieBySalt(String saltHex) {
        UserCookie userCookie = null;
        if (sharePre == null) {
            getInstance();
        }
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

    /******************************  Random Cookie  ********************************************************/
    public void insertRandomCookie(UserCookie userCookie) {
        String randomCookieKey = COOKIE_RANDOM;
        putValue(randomCookieKey, new Gson().toJson(userCookie));
    }

    public UserCookie loadRandomCookie() {
        String randomCookieKey = COOKIE_RANDOM;
        UserCookie userCookie = null;
        if (sharePre == null) {
            getInstance();
        }
        if (sharePre.contains(randomCookieKey)) {
            String gsonCookie = sharePre.getString(randomCookieKey, "");
            if (!TextUtils.isEmpty(gsonCookie)) {
                userCookie = new Gson().fromJson(gsonCookie, UserCookie.class);
            }
        }
        return userCookie;
    }

    /******************************  Friend Cookie  ********************************************************/
    public void insertFriendCookie(String friendCaPublicKey, UserCookie userCookie) {
        String friendCookieKey = COOKIE_CHATFRIEND + friendCaPublicKey;
        if (sharePre == null) {
            getInstance();
        }
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
        if (sharePre == null) {
            getInstance();
        }
        if (sharePre.contains(friendCookieKey)) {
            String gsonCookies = sharePre.getString(friendCookieKey, "");
            if (!TextUtils.isEmpty(gsonCookies)) {
                List<UserCookie> userCookieList = new Gson().fromJson(gsonCookies, new TypeToken<List<UserCookie>>() {
                }.getType());

                int cookiesLength = userCookieList.size();
                userCookie = userCookieList.get(cookiesLength - 1);
            }
        }
        return userCookie;
    }


    /******************************  Group Member Cookie  ********************************************************/
    public void insertGroupMemberCookie(String groupIdentify, String groupMemberUid, UserCookie userCookie) {
        String groupMemberCookieKey = COOKIE_CHATGROUP_MEMBER + groupIdentify + groupMemberUid;
        if (sharePre == null) {
            getInstance();
        }
        if (sharePre.contains(groupMemberCookieKey)) {
            String gsonCookies = sharePre.getString(groupMemberCookieKey, "");
            if (TextUtils.isEmpty(gsonCookies)) {
                remove(groupMemberCookieKey);
                insertGroupMemberCookie(groupIdentify, groupMemberUid, userCookie);
            } else {
                List<UserCookie> userCookieList = new Gson().fromJson(gsonCookies, new TypeToken<List<UserCookie>>() {
                }.getType());
                if (userCookieList.size() >= 3) {
                    int cookiesLength = userCookieList.size();
                    userCookieList = userCookieList.subList(cookiesLength - 2, cookiesLength);
                }
                userCookieList.add(userCookie);
                putValue(groupMemberCookieKey, new Gson().toJson(userCookieList));
            }
        } else {
            List<UserCookie> userCookieList = new ArrayList<>();
            userCookieList.add(userCookie);
            putValue(groupMemberCookieKey, new Gson().toJson(userCookieList));
        }
    }

    public UserCookie loadGroupMemberCookie(String groupIdentify, String groupMemberUid) {
        String groupMemberCookieKey = COOKIE_CHATGROUP_MEMBER + groupIdentify + groupMemberUid;
        UserCookie userCookie = null;
        if (sharePre == null) {
            getInstance();
        }
        if (sharePre.contains(groupMemberCookieKey)) {
            String gsonCookies = sharePre.getString(groupMemberCookieKey, "");
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
