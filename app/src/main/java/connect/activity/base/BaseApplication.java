package connect.activity.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.List;

import connect.activity.chat.model.EmoManager;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.instant.receiver.CommandReceiver;
import connect.instant.receiver.ConnectReceiver;
import connect.instant.receiver.MessageReceiver;
import connect.instant.receiver.RobotReceiver;
import connect.instant.receiver.TransactionReceiver;
import connect.instant.receiver.UnreachableReceiver;
import connect.service.UpdateInfoService;
import connect.utils.ConfigUtil;
import instant.parser.localreceiver.CommandLocalReceiver;
import instant.parser.localreceiver.ConnectLocalReceiver;
import instant.parser.localreceiver.MessageLocalReceiver;
import instant.parser.localreceiver.RobotLocalReceiver;
import instant.parser.localreceiver.TransactionLocalReceiver;
import instant.parser.localreceiver.UnreachableLocalReceiver;
import instant.ui.InstantSdk;

/**
 * Created by john on 2016/11/19.
 */

public class BaseApplication extends Application{

    private static BaseApplication mApplication = null;
    private static List<Activity> activityList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;

        InstantSdk.instantSdk.initSdk(this);
        EmoManager.getInstance();
        UnCeHandler catchExcep = new UnCeHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(catchExcep);
        CrashReport.initCrashReport(this, ConfigUtil.getInstance().getCrashAPPID(), true);
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

    public Context getAppContext() {
        return getApplicationContext();
    }

    public List<Activity> getActivityList() {
        return activityList;
    }

    public boolean isEmptyActivity() {
        return activityList == null || activityList.size() == 0;
    }

    public void finishActivity(){
        for (Activity activity : activityList) {
            if (null != activity) {
                activity.finish();
            }
        }

        InstantSdk.instantSdk.stopInstant();
        UpdateInfoService.stopServer(this.getAppContext());
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    private void initInstantSDK() {
        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        InstantSdk.instantSdk.registerUserInfo(mApplication, userBean.getPubKey(), userBean.getPriKey());

        ConnectLocalReceiver.receiver.registerConnect(ConnectReceiver.receiver);
        CommandLocalReceiver.receiver.registerCommand(CommandReceiver.receiver);
        TransactionLocalReceiver.localReceiver.registerTransactionListener(TransactionReceiver.receiver);
        RobotLocalReceiver.localReceiver.registerRobotListener(RobotReceiver.receiver);
        UnreachableLocalReceiver.localReceiver.registerUnreachableListener(UnreachableReceiver.receiver);
        MessageLocalReceiver.localReceiver.registerMessageListener(MessageReceiver.receiver);
    }

}
