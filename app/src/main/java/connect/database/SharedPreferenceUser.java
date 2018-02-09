package connect.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;

import connect.activity.base.BaseApplication;
import connect.activity.login.bean.CaPubBean;

/**
 * Created by Administrator on 2017/11/21 0021.
 */

public class SharedPreferenceUser {

    private static SharedPreferenceUser sharePreUtil;
    private static SharedPreferences sharePre;

    /** user ca pubkey*/
    public static final String USER_CAPUB = "user_capub";

    public static SharedPreferenceUser getInstance(String uid) {
        if (null == sharePreUtil) {
            sharePreUtil = new SharedPreferenceUser();
            sharePre = BaseApplication.getInstance().getBaseContext().getSharedPreferences(uid, Context.MODE_PRIVATE);
        }
        return sharePreUtil;
    }

    /**
     * Save the ca Information
     * @param caPubBean
     */
    public void putCaPubBean(CaPubBean caPubBean) {
        SharedPreferences.Editor editor = sharePre.edit();
        editor.putString(USER_CAPUB, new Gson().toJson(caPubBean));
        editor.apply();
    }

    public CaPubBean getCaPubBean() {
        String value = sharePre.getString(USER_CAPUB, "");
        if(TextUtils.isEmpty(value)){
            return new CaPubBean();
        }
        return new Gson().fromJson(value, CaPubBean.class);
    }

}
