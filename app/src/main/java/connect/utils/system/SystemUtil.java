package connect.utils.system;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import connect.activity.base.BaseApplication;

import static android.content.Context.ACTIVITY_SERVICE;

public class SystemUtil {

    /**
     * dp to px
     * @param dpValue
     * @return
     */
    public static int dipToPx(float dpValue) {
        Context context = BaseApplication.getInstance().getBaseContext();
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px to dp
     * @param pxValue
     * @return
     */
    public static int pxToDip(float pxValue) {
        Context context = BaseApplication.getInstance().getBaseContext();
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * px to sp
     * @param size
     * @return
     */
    public static float pxToSp(float size) {
        Context context = BaseApplication.getInstance().getBaseContext();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size,
                context.getResources().getDisplayMetrics());
    }

    /**
     * sp to px
     * @param spValue
     * @return
     */
    public static int spToPx(float spValue) {
        Context context = BaseApplication.getInstance().getBaseContext();
        return (int) (spValue * context.getResources().getDisplayMetrics().scaledDensity + 0.5f);
    }

    /**
     * Display keyboard (interface not loaded completely to the pop-up soft keyboard Should be appropriate delay)
     * @param context
     * @param editText
     */
    public static void showKeyBoard(final Context context, final EditText editText){
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            public void run(){
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
            }
        }, 300);
    }

    /**
     * Hide the keyboard
     * @param context
     * @param editText
     */
    public static void hideKeyBoard(Context context,EditText editText){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        editText.clearFocus();
    }

    public static boolean isSoftShowing(Activity activity) {
        final int softKeyboardHeight = 100;
        View rootView = activity.getWindow().getDecorView();
        Rect r = new Rect();
        rootView.getWindowVisibleDisplayFrame(r);
        DisplayMetrics dm = rootView.getResources().getDisplayMetrics();
        int heightDiff = rootView.getBottom() - r.bottom;
        return heightDiff > softKeyboardHeight * dm.density;
    }

    /**
     * view Location in the screen
     * @param view
     * @return
     */
    public static int[] locationOnScreen(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location;
    }

    /**
     * sendPhoneSMS Send short messages
     * @return Object
     * @Exception
     */
    public static void sendPhoneSMS(Context context, String phoneNumber, String inviteText) {
        Uri smsToUri = Uri.parse("smsto:" + phoneNumber);
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra("sms_body", inviteText);
        context.startActivity(intent);
    }

    /**
     * play system sound
     * @param context
     */
    public static void noticeVoice(Context context) {
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        if (r != null) {
            r.play();
        }
    }

    /**
     * play system vibration
     * @param context
     */
    public static void noticeVibrate(Context context) {
        Vibrator vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(100);
    }

    /**
     * Phone call
     * @param phonenum
     */
    public static void callPhone(Context context, String phonenum) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phonenum));
        context.startActivity(intent);
    }

    /**
     * Does App run in the background
     */
    public static boolean isRunBackGround() {
        Context context = BaseApplication.getInstance().getBaseContext();
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses != null) {
            //Attempt to invoke interface method 'java.util.Iterator java.util.List.iterator()' on a null object reference
            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.processName.equals(context.getPackageName())) {
                    return appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND;
                }
            }
        }
        return false;
    }

    /**
     * Determine whether to open the Wifi
     * @return
     */
    public static boolean isOpenWifi(){
        Context context = BaseApplication.getInstance().getBaseContext();
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return networkInfo.isConnected();
    }

    public static int getStateBarHeight() {
        int statusBarHeight1 = -1;
        Context context = BaseApplication.getInstance().getBaseContext();
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = context.getResources().getDimensionPixelSize(resourceId);
        } else {
            statusBarHeight1 = dipToPx(50);
        }
        return statusBarHeight1;
    }
}
