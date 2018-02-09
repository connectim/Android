package connect.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;

import java.util.Locale;

import connect.activity.base.BaseApplication;
import connect.activity.login.bean.UserBean;
import connect.utils.exception.BaseException;
import connect.utils.exception.bean.ErrorCode;
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

    public SharedPreferenceUtil() {}

    public static SharedPreferenceUtil getInstance() {
        return getInstance(BaseApplication.getInstance().getBaseContext());
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
    public UserBean getUser() {
        UserBean userBean = null;
        try {
            String userStr = getStringValue(USER_INFO);
            if (TextUtils.isEmpty(userStr)) {
                throw new BaseException(ErrorCode.USER_NOT_EXIST);
            }

            userBean = new Gson().fromJson(userStr, UserBean.class);
        } catch (BaseException e) {
            e.dispath().upload();
        }
        return userBean;
    }

    public UserBean getUserCheckExist() {
        UserBean userBean = null;
        try {
            String userStr = getStringValue(USER_INFO);
            if (TextUtils.isEmpty(userStr)) {
                throw new BaseException(ErrorCode.USER_NOT_EXIST);
            }

            userBean = new Gson().fromJson(userStr, UserBean.class);
            if (userBean == null || TextUtils.isEmpty(userBean.getUid())) {
                throw new BaseException(ErrorCode.USER_NOT_EXIST);
            }
        } catch (BaseException e) {
            e.dispath().upload();
        }
        return userBean;
    }

    public boolean containsUser() {
        boolean isContain = true;
        String userStr = getStringValue(USER_INFO);
        if(TextUtils.isEmpty(userStr)){
            isContain = false;
        }else{
            UserBean userBean = new Gson().fromJson(userStr, UserBean.class);
            if(userBean == null || TextUtils.isEmpty(userBean.getUid())){
                isContain = false;
            }
        }
        return isContain;
    }

    public String getLanguageCode(){
        String languageCode = getStringValue(SharedPreferenceUtil.APP_LANGUAGE_CODE);
        if(TextUtils.isEmpty(languageCode)){
            languageCode = Locale.getDefault().getLanguage();
        }else if(languageCode.equals("zh")){
            languageCode = Locale.SIMPLIFIED_CHINESE.getLanguage();
        }else if(languageCode.equals("ru")){
            languageCode = new Locale("ru","RU").getLanguage();
        }else{
            languageCode = Locale.ENGLISH.getLanguage();
        }
        return languageCode;
    }

    public void clear(){
        if (sharePre == null) {
            getInstance();
        }

        sharePre.edit()
                .clear()
                .apply();
        sharePre = null;
    }
}
