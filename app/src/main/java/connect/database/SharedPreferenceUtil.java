package connect.database;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import connect.activity.home.bean.EstimateFeeBean;
import connect.activity.login.bean.UserBean;
import connect.activity.base.BaseApplication;
import connect.utils.log.LogManager;

/**
 * save App information
 * Created by john on 2016/11/19.
 */

public class SharedPreferenceUtil {
    private static SharedPreferenceUtil sharePreUtil;
    private static SharedPreferences sharePre;

    public static final String SP_NAME = "sp_name";
    public static final String START_IMAGES_HASH = "start_hash";
    public static final String START_IMAGES_ADDRESS = "start_images";
    public static final String FIRST_INTO_APP = "first_into_app";
    public static final String WEB_OPEN_APP = "web_open_app";
    public static final String APP_LANGUAGE_CODE = "app_language_code";
    public static final String USER_INFO_LIST = "user_info_list";
    public static final String USER_INFO = "user_info";
    public static final String ESTIMATE_FEE = "esti_fee";
    public static final String ROOM_TYPE = "ROOM_TYPE";
    public static final String ROOM_KEY = "ROOM_KEY";
    public static final String ROOM_ECDH = "ROOM_ECDH";
    public static final String USER_LIST_ADD = "ADD";
    public static final String USER_LIST_DEL = "DEL";

    /** user SharedPreference name */
    public static final String SHAREPRE_NAME = "SHAREPRE_NAME";

    public SharedPreferenceUtil() {

    }

    public static String Tag = "SharedPreferenceUtil";

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

    public String getStringValue(String key) {
        return sharePre.getString(key, "");
    }

    public boolean isContains(String key) {
        return sharePre.contains(key);
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.remove(key);
        editor.apply();
    }

    /**
     * save current user
     * @param user
     */
    public void putUser(UserBean user) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putString(USER_INFO,new Gson().toJson(user));
        editor.apply();
        //putUserList(user,USER_LIST_ADD);
        MemoryDataManager.getInstance().initMemoryData();
    }

    /**
     * get current user
     * @return
     */
    public UserBean getUser(){
        UserBean userBean = new Gson().fromJson(getStringValue(USER_INFO), UserBean.class);
        return userBean;
    }

    /**
     * save local user
     * @param user
     * @param status add del
     */
    public void putUserList(UserBean user,String status) {
        user.setPriKey("");
        user.setSalt("");
        ArrayList<UserBean> listUser = getUserList();
        if(listUser == null){
            listUser = new ArrayList<>();
        }else{
            for(UserBean userBean : listUser){
                if(userBean.getPubKey().equals(user.getPubKey())){
                    listUser.remove(userBean);
                    break;
                }
            }
        }
        if(status.equals(USER_LIST_ADD)){
            listUser.add(0,user);
        }

        SharedPreferences.Editor editor = sharePre.edit();
        editor.putString(USER_INFO_LIST,new Gson().toJson(listUser));
        editor.apply();
    }

    /**
     * Save the user information in hand in the interface
     *
     * @param userBean The user information
     */
    public void loginSaveUserBean(UserBean userBean, Activity currentActivity) {
        // Save the current login user information
        SharedPreferenceUtil.getInstance().putUser(userBean);
        // finish the Activity except the current Activity
        List<Activity> list = BaseApplication.getInstance().getActivityList();
        for (Activity activity : list) {
            if (!activity.getClass().getName().equals(currentActivity.getClass().getName())) {
                activity.finish();
            }
        }
        // Save the current user private key to memory
        MemoryDataManager.getInstance().putPriKey(userBean.getPriKey());
    }

    /**
     * get local user
     * @return
     */
    public ArrayList<UserBean> getUserList(){
        Type type = new TypeToken<ArrayList<UserBean>>() {}.getType();
        return new Gson().fromJson(getStringValue(USER_INFO_LIST), type);
    }

    /**
     * save estimate fee
     */
    public void putEstimatefee(EstimateFeeBean estimatefeeBean) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putString(ESTIMATE_FEE,new Gson().toJson(estimatefeeBean));
        editor.apply();
    }

    /**
     * get estimate fee
     * @return
     */
    public EstimateFeeBean getEstimatefee(){
        return new Gson().fromJson(getStringValue(ESTIMATE_FEE), EstimateFeeBean.class);
    }

}
