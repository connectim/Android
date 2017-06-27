package connect.activity.base;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import java.util.ArrayList;
import java.util.List;

import connect.service.HttpsService;
import connect.service.SocketService;

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

        /*UnCeHandler catchExcep = new UnCeHandler(this);
        Thread.setDefaultUncaughtExceptionHandler(catchExcep);
        CrashReport.initCrashReport(getApplicationContext(),
                ConfigUtil.getInstance().appMode() ?
                        "9b63c64ee1" : "cf78e82d4f", true);*/
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

        SocketService.stopServer(this.getAppContext());
        HttpsService.stopServer(this.getAppContext());
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

}
