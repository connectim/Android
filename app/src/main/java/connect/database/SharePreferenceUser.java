package connect.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import connect.activity.base.BaseApplication;
import connect.activity.wallet.bean.AddressBean;
import connect.activity.wallet.bean.WalletBean;
import connect.utils.log.LogManager;

/**
 * save different user information
 */
public class SharePreferenceUser {

    private static String Tag="_SharePreferenceUser";
    private static SharePreferenceUser sharePreUtil;
    private static SharedPreferences sharePre;
    public static final String USER_ADDRESS_BOOK = "user_address_book";
    public static final String DB_PUBKEY = "db_pubkey";
    public static final String DB_SALT = "db_salt";
    public static final String WALLET_INFO = "wallet_info";

    public static void initSharePreference(String pubKey) {
        sharePreUtil = null;
        SharedPreferenceUtil.getInstance().putValue(SharedPreferenceUtil.SHAREPRE_NAME, "sp_" + pubKey);
        getInstance(BaseApplication.getInstance().getAppContext());
    }

    public static void unLinkSharePreference() {
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

    /**
     * save user wallet address
     * @param list
     */
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

    /**
     * save wallet setting information
     * @param walletBean
     */
    public void putWalletInfo(WalletBean walletBean){
        String value = new Gson().toJson(walletBean);
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putString(WALLET_INFO, value);
        editor.apply();
    }

    public WalletBean getWalletInfo() {
        String value = getStringValue(WALLET_INFO);
        if(TextUtils.isEmpty(value)){
            return null;
        }
        Type type = new TypeToken<WalletBean>() {}.getType();
        return new Gson().fromJson(value, type);
    }

}
