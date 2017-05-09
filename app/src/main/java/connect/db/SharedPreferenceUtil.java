package connect.db;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import connect.ui.activity.home.bean.EstimatefeeBean;
import connect.ui.activity.login.bean.UserBean;
import connect.ui.base.BaseApplication;
import connect.utils.log.LogManager;

/**
 * save App information
 * Created by john on 2016/11/19.
 */

public class SharedPreferenceUtil {

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

    private static SharedPreferenceUtil sharePreUtil;
    private static SharedPreferences sharePre;

    /** user PRI_KEY */
    public static final String KEY_PRIVATE = "KEY_PRIVATE";
    /** user PUB_KEY */
    public static final String KEY_PUBLIC = "KEY_PUBLIC";
    /** user PUB_ADDRESS */
    public static final String PUB_ADDRESS = "PUB_ADDRESS";
    /** user AVATAR */
    public static final String PUB_AVATAR = "PUB_AVATAR";
    /** user SharedPreference name */
    public static final String SHAREPRE_NAME = "SHAREPRE_NAME";

    private Map<String, String> stringMap = null;

    public SharedPreferenceUtil() {
        stringMap = new HashMap();
    }

    public static String Tag = "SharedPreferenceUtil";

    public static SharedPreferenceUtil getInstance() {
        return getInstance(BaseApplication.getInstance().getAppContext());
    }

    private static SharedPreferenceUtil getInstance(Context context) {
        if (null == sharePreUtil) {
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

    public void putValue(String key, boolean value) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public String getStringValue(String key) {
        return sharePre.getString(key, "");
    }

    public int getIntValue(String key) {
        return sharePre.getInt(key, 0);
    }

    public boolean getBooleanValue(String key) {
        return sharePre.getBoolean(key, false);
    }

    public boolean isContains(String key) {
        return sharePre.contains(key);
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.remove(key);
        editor.apply();
    }

    public void updataUser(UserBean userBean){
        putUser(userBean);
        putUserList(userBean);
    }

    public void putUser(UserBean user) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putString(USER_INFO,new Gson().toJson(user));
        editor.apply();
    }

    public UserBean getUser(){
        UserBean userBean = new Gson().fromJson(getStringValue(USER_INFO), UserBean.class);
        String priKey = sharePreUtil.getPriKey();
        if(userBean != null && !TextUtils.isEmpty(priKey)){
            userBean.setPriKey(priKey);
        }
        return userBean;
    }

    /**
     * save local user
     */
    public void putUserList(UserBean user) {
        UserBean userBeanLocal = new UserBean();
        userBeanLocal.setPubKey(user.getPubKey());
        userBeanLocal.setName(user.getName());
        userBeanLocal.setAvatar(user.getAvatar());
        userBeanLocal.setTalkKey(user.getTalkKey());
        userBeanLocal.setPhone(user.getPhone());
        userBeanLocal.setPassHint(user.getPassHint());
        userBeanLocal.setConnectId(user.getConnectId());
        userBeanLocal.setBack(user.isBack());

        ArrayList<UserBean> listUser = getUserList();
        if(listUser == null){
            listUser = new ArrayList<>();
            listUser.add(userBeanLocal);
        }else{
            for(UserBean userBean : listUser){
                if(userBean.getPubKey().equals(userBeanLocal.getPubKey())){
                    listUser.remove(userBean);
                    break;
                }
            }
            listUser.add(0,userBeanLocal);
        }
        putUserList(listUser);
    }

    public void putUserList(ArrayList<UserBean> listUserBean) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putString(USER_INFO_LIST,new Gson().toJson(listUserBean));
        editor.apply();
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
     * estimate fee
     */
    public void putEstimatefee(EstimatefeeBean estimatefeeBean) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putString(ESTIMATE_FEE,new Gson().toJson(estimatefeeBean));
        editor.apply();
    }
    public EstimatefeeBean getEstimatefee(){
        return new Gson().fromJson(getStringValue(ESTIMATE_FEE), EstimatefeeBean.class);
    }

    /**=====================  Memory data management  ======================*/
    public void initPutMapStr(String priKey, String pubKey, String address, String avatar) {
        putMapStr(KEY_PRIVATE, priKey);
        putMapStr(KEY_PUBLIC, pubKey);
        putMapStr(PUB_ADDRESS, address);
        putMapStr(PUB_AVATAR, avatar);
    }

    public void putMapStr(String key,String value){
        if(stringMap.containsKey(key)){
            stringMap.remove(key);
        }
        stringMap.put(key,value);
    }

    public String getPriKey() {
        String prikey = stringMap.get(KEY_PRIVATE);
        if (TextUtils.isEmpty(prikey)) {
            UserBean userBean = new Gson().fromJson(getStringValue(USER_INFO), UserBean.class);
            if (userBean != null && TextUtils.isEmpty(userBean.getSalt())) {
                prikey = userBean.getPriKey();
            }
        }
        return prikey;
    }

    public boolean isAvailableKey() {
        String priKey = getPriKey();
        return !TextUtils.isEmpty(priKey);
    }

    public String getPubKey() {
        String pubkey = stringMap.get(KEY_PUBLIC);
        if (TextUtils.isEmpty(pubkey)) {
            UserBean user = getUser();
            pubkey = (null == user) ? "" : user.getPubKey();
            stringMap.put(KEY_PUBLIC, pubkey);
        }
        if(TextUtils.isEmpty(pubkey)){
            BaseApplication.getInstance().finishActivity();
        }
        return pubkey;
    }

    public String getAvatar() {
        String avatar = stringMap.get(PUB_AVATAR);
        if (TextUtils.isEmpty(avatar)) {
            UserBean user = getUser();
            avatar = (null == user) ? "" : user.getAvatar();
            stringMap.put(PUB_AVATAR, avatar);
        }
        return avatar;
    }

    public String getAddress() {
        String address = stringMap.get(PUB_ADDRESS);
        if (TextUtils.isEmpty(address)) {
            UserBean user = getUser();
            address = (null == user) ? "" : user.getAddress();
            stringMap.put(PUB_ADDRESS, address);
        }
        return address;
    }

    public void clearMap() {
        if (stringMap != null) {
            stringMap.clear();
        }
    }
}
