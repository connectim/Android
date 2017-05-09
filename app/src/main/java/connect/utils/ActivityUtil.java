package connect.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import connect.ui.activity.R;
import connect.ui.base.BaseApplication;

public class ActivityUtil {

    public static void next(Activity curActivity, Class nextActivity) {
        next(curActivity, nextActivity, null);
    }

    public static void next(Activity curActivity, Class nextActivity, int reqCode) {
        next(curActivity, nextActivity, null, reqCode);
    }

    public static void next(Activity curActivity, Class nextActivity, Bundle extras) {
        next(curActivity, nextActivity, extras, 0);
    }

    public static void next(Activity curActivity, Class nextActivity, Bundle extras, int reqCode) {
        next(curActivity, nextActivity, extras, reqCode, R.anim.activity_in_from_right, R.anim.activity_0_to_0);
    }

    public static void next(Activity curActivity, Class nextActivity, int inAnimId, int outAnimId) {
        next(curActivity, nextActivity, null, 0, inAnimId, outAnimId);
    }

    public static void nextBottomToTop(Activity curActivity, Class nextActivity, Bundle extras, int reqCode) {
        next(curActivity, nextActivity, extras, reqCode, R.anim.dialog_bottom_show, R.anim.activity_0_to_0);
    }

    private static void next(Activity curActivity, Class nextActivity, Bundle extras, int reqCode, int inAnimId,
                             int outAnimId) {
        Intent intent = new Intent(curActivity, nextActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (null != extras) {
            intent.putExtras(extras);
        }
        if (reqCode <= 0) {
            curActivity.startActivity(intent);
        } else {
            curActivity.startActivityForResult(intent, reqCode);
        }
        curActivity.overridePendingTransition(inAnimId,outAnimId);
    }

    public static void goBack(Activity curActivity) {
        goBackWithResult(curActivity, 0,null);
    }

    public static void goBackBottom(Activity curActivity) {
        goBackWithResult(curActivity, 0,null,R.anim.activity_0_to_0,R.anim.dialog_bottom_dismiss);
    }

    public static void goBackWithResult(Activity curActivity, int retCode, Bundle retData) {
        goBackWithResult(curActivity, retCode, retData, R.anim.activity_0_to_0,R.anim.activity_out_to_right);
    }

    public static void goBackWithResult(Activity curActivity, int retCode, Bundle retData, int inAnimId, int outAnimId) {
        Intent intent = null;
        intent = new Intent();
        if (null != retData) {
            intent.putExtras(retData);
        }
        curActivity.setResult(retCode, intent);
        curActivity.finish();
        curActivity.overridePendingTransition(inAnimId, outAnimId);
    }

    public static void backActivityWithClearTop(Activity curActivity, Class backActivity) {
        backActivityWithClearTop(curActivity, backActivity, null, R.anim.activity_in_from_left, R.anim.activity_out_to_right);
    }

    public static void backActivityWithClearTop(Activity curActivity, Class backActivity, Bundle extras, int inAnimId, int outAnimId) {
        Intent intent = new Intent(curActivity, backActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (null != extras) {
            intent.putExtras(extras);
        }
        curActivity.startActivity(intent);
        curActivity.overridePendingTransition(inAnimId, outAnimId);
    }

    public static String getRunningActivityName() {
        Context context = BaseApplication.getInstance().getAppContext();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
    }

    public static boolean isRunChatActivity(String acyname) {
        String runAcy = getRunningActivityName();
        return acyname.equals(runAcy);
    }
}
