package connect.database;

import android.content.Context;
import android.content.SharedPreferences;

import connect.activity.wallet.bean.AddressBean;
import connect.activity.base.BaseApplication;
import connect.utils.log.LogManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * save different user information
 * Created by Administrator on 2016/12/8.
 */
public class SharePreferenceUser {

    private static String Tag="SharePreferenceUser";

    public static final String USER_ADDRESS_BOOK = "user_address_book";
    public static final String CONTACT_VERSION = "CONTACT_VERSION";
    public static final String DB_PUBKEY = "db_pubkey";
    public static final String DB_SALT = "db_salt";
    public static final String BASE_SEED = "base_seed";

    private static SharePreferenceUser sharePreUtil;
    private static SharedPreferences sharePre;

    public static void initSharePreferrnce(String pubkey) {
        sharePreUtil = null;
        SharedPreferenceUtil.getInstance().putValue(SharedPreferenceUtil.SHAREPRE_NAME, "sp_" + pubkey);
        getInstance(BaseApplication.getInstance().getAppContext());
    }

    public static void unLinkSharePreferrnce() {
        sharePreUtil = null;
        sharePre = null;
    }

    public static SharePreferenceUser getInstance() {
        return getInstance(BaseApplication.getInstance().getAppContext());
    }

    private static SharePreferenceUser getInstance(Context context) {
        if (null == sharePreUtil) {
            sharePreUtil = new SharePreferenceUser();

            String shareName = SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.SHAREPRE_NAME);
            LogManager.getLogger().d(Tag, "*** SP_NAME :" + shareName);
            sharePre = context.getSharedPreferences(shareName, Context.MODE_PRIVATE);
        }
        return sharePreUtil;
    }

    public void remove(String key) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.remove(key);
        editor.apply();
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void putInt(String key, int value) {
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

    public void putAddressBook(ArrayList list) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putString(USER_ADDRESS_BOOK, new Gson().toJson(list));
        editor.apply();
    }

    public ArrayList<AddressBean> getAddressBook() {
        Type type = new TypeToken<ArrayList<AddressBean>>() {
        }.getType();
        return new Gson().fromJson(getStringValue(USER_ADDRESS_BOOK), type);
    }
}
