package connect.activity.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.List;

import connect.activity.base.inter.InterAccount;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoManager;
import connect.instant.receiver.ReceiverHelper;
import connect.service.GroupService;
import connect.service.UpdateInfoService;
import connect.utils.ConfigUtil;
import connect.utils.FileUtil;
import connect.utils.ProgressUtil;
import connect.widget.bottominput.EmoManager;
import instant.ui.InstantSdk;

/**
 * Created by john on 2016/11/19.
 */
public class BaseApplication extends Application implements InterAccount {

    private static BaseApplication mApplication = null;
    private static List<Activity> activityList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;

        // IM SDK
        InstantSdk.getInstance().initSdk(this);
        // EMOJI
        EmoManager.emojiManager.initEmojiResource();

        String appId = ConfigUtil.getInstance().getCrashAPPID();
        CrashReport.initCrashReport(this, appId, true);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static BaseApplication getInstance() {
        if (mApplication == null) {
            mApplication = new BaseApplication();
        }
        return mApplication;
    }

    public List<Activity> getActivityList() {
        return activityList;
    }

    public boolean isEmptyActivity() {
        return activityList == null || activityList.size() == 0;
    }

    @Override
    public void initRegisterAccount() {
        FileUtil.getExternalStorePath();

        // IM SDK
        new ReceiverHelper().initInstantSDK();

        // Bugly
        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        CrashReport.setUserId(userBean.getUid());
        CrashReport.setUserSceneTag(this.getBaseContext(), Integer.valueOf(ConfigUtil.getInstance().getCrashTags()));
    }

    @Override
    public void exitRegisterAccount() {
        //SDK
        InstantSdk.getInstance().stopInstant();

        DaoManager.getInstance().closeDataBase();

        //service
        UpdateInfoService.stopServer(this.getApplicationContext());
        GroupService.stopServer(this.getApplicationContext());

        ProgressUtil.getInstance().dismissProgress();

        //Remove the local login information
//        UserBean userBean = SharedPreferenceUtil.getInstance().getUserCheckExist();
//        userBean.setUid("");
//        userBean.setToken("");
//        SharedPreferenceUtil.getInstance().putUser(userBean);
    }
}
