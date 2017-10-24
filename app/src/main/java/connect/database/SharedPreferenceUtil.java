package connect.database;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import connect.activity.base.BaseApplication;
import connect.activity.login.bean.UserBean;
import connect.utils.log.LogManager;

/**
 * save App information
 */

public class SharedPreferenceUtil {

    private static SharedPreferenceUtil sharePreUtil;
    private static SharedPreferences sharePre;
    public static String Tag = "_SharedPreferenceUtil";
    public static final String SHAREPRE_NAME = "SHAREPRE_NAME";
    public static final String SP_NAME = "sp_name";
    public static final String START_IMAGES_HASH = "start_hash";
    public static final String START_IMAGES_ADDRESS = "start_images";
    public static final String FIRST_INTO_APP = "first_into_app";
    public static final String WEB_OPEN_APP = "web_open_app";
    public static final String APP_LANGUAGE_CODE = "app_language_code";
    public static final String USER_INFO = "user_info";
    public static final String ROOM_TYPE = "ROOM_TYPE";
    public static final String ROOM_KEY = "ROOM_KEY";
    public static final String ROOM_ECDH = "ROOM_ECDH";

    public SharedPreferenceUtil() {}

    public static SharedPreferenceUtil getInstance() {
        return getInstance(BaseApplication.getInstance().getAppContext());
    }

    private static SharedPreferenceUtil getInstance(Context context) {
        if (null == sharePreUtil || null == sharePre) {
            sharePreUtil = new SharedPreferenceUtil();
            sharePre = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            LogManager.getLogger().d(Tag, "*** SP_NAME :" + SP_NAME);
        }
        return sharePreUtil;
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

    public boolean isContains(String key) {
        return sharePre.contains(key);
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.remove(key);
        editor.apply();
    }

    public String getStringValue(String key) {
        return sharePre.getString(key, "");
    }

    /**
     * save current user
     * @param user
     */
    public void putUser(UserBean user) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putString(USER_INFO,new Gson().toJson(user));
        editor.apply();
    }

    /**
     * get current user
     * @return
     */
    public UserBean getUser(){
        UserBean userBean = new Gson().fromJson(getStringValue(USER_INFO), UserBean.class);
        return userBean;
    }

}
