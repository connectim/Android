package connect.database;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import connect.activity.login.bean.UserBean;
import connect.activity.base.BaseApplication;

/**
 * Temporary data stored in the memory management
 */

public class MemoryDataManager {

    private static MemoryDataManager dataManager;
    private Map<String, String> stringMap = null;
    /** user PRI_KEY */
    public static final String KEY_PRIVATE = "KEY_PRIVATE";
    /** user PUB_KEY */
    public static final String KEY_PUBLIC = "KEY_PUBLIC";
    /** user PUB_ADDRESS */
    public static final String PUB_ADDRESS = "PUB_ADDRESS";
    /** user AVATAR */
    public static final String PUB_AVATAR = "PUB_AVATAR";
    /** user NAME */
    public static final String PUB_NAME = "PUB_NAME";

    public MemoryDataManager() {
        stringMap = new HashMap();
    }

    public static MemoryDataManager getInstance() {
        if (null == dataManager) {
            dataManager = new MemoryDataManager();
        }
        return dataManager;
    }

    public void putMapStr(String key,String value){
        if(stringMap.containsKey(key)){
            stringMap.remove(key);
        }
        stringMap.put(key,value);
    }

    /**
     * When modified the user profiles need to empty the memory data, and then synchronize the latest data
     */
    public void initMemoryData(){
        putMapStr(PUB_AVATAR,"");
        putMapStr(PUB_NAME,"");
    }

    /**
     * Save the current user's private key
     * @param priKey
     */
    public void putPriKey(String priKey){
        putMapStr(KEY_PRIVATE,priKey);
    }

    /**
     * Gets the current user's private key
     */
    public String getPriKey() {
        String prikey = stringMap.get(KEY_PRIVATE);
        if (TextUtils.isEmpty(prikey)) {
            UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
            if (userBean != null && TextUtils.isEmpty(userBean.getSalt())) {
                prikey = userBean.getPriKey();
                stringMap.put(KEY_PRIVATE, prikey);
            }
        }
        return prikey;
    }

    /**
     * Gets the current user's public key
     * @return
     */
    public String getPubKey() {
        String pubkey = stringMap.get(KEY_PUBLIC);
        if (TextUtils.isEmpty(pubkey)) {
            UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
            pubkey = (null == userBean) ? "" : userBean.getPubKey();
            stringMap.put(KEY_PUBLIC, pubkey);
        }
        if(TextUtils.isEmpty(pubkey)){
            BaseApplication.getInstance().finishActivity();
        }
        return pubkey;
    }

    /**
     * Gets the current user's name
     * @return
     */
    public String getName() {
        String name = stringMap.get(PUB_NAME);
        if (TextUtils.isEmpty(name)) {
            UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
            name = (null == userBean) ? "" : userBean.getName();
            stringMap.put(PUB_NAME, name);
        }
        return name;
    }

    /**
     * Gets the current user's avatar
     * @return
     */
    public String getAvatar() {
        String avatar = stringMap.get(PUB_AVATAR);
        if (TextUtils.isEmpty(avatar)) {
            UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
            avatar = (null == userBean) ? "" : userBean.getAvatar();
            stringMap.put(PUB_AVATAR, avatar);
        }
        return avatar;
    }

    /**
     * Gets the current user's address
     * @return
     */
    /*public String getAddress() {
        String address = stringMap.get(PUB_ADDRESS);
        if (TextUtils.isEmpty(address)) {
            UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
            address = (null == userBean) ? "" : userBean.getAddress();
            stringMap.put(PUB_ADDRESS, address);
        }
        return address;
    }*/

    /**
     * Is there a current user private key
     * @return
     */
    public boolean isAvailableKey() {
        String priKey = getPriKey();
        return !TextUtils.isEmpty(priKey);
    }

    /**
     * Clear memory temporarily store data
     */
    public void clearMap() {
        if (stringMap != null) {
            stringMap.clear();
        }
    }

}
