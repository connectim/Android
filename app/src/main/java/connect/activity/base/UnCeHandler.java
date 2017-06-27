package connect.activity.base;

import android.os.Looper;
import android.util.Log;

/**
 * Created by Administrator on 2017/3/8.
 */

public class UnCeHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultHandler;
    public static final String TAG = "CatchExcep";
    BaseApplication application;

    public UnCeHandler(BaseApplication application){
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.application = application;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if(!handleException(ex) && mDefaultHandler != null){
            mDefaultHandler.uncaughtException(thread, ex);
        }else{
            ex.printStackTrace();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }
            Log.e("app error",ex.getMessage());
            application.finishActivity();
        }

        /*AlarmManager mgr = (AlarmManager) application.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(application, StartActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("crash", true);
        PendingIntent restartIntent = PendingIntent.getActivity(application, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent);*/
    }

    /**
     * Custom error handling, collection of error messages, error reporting, etc.
     *
     * @param ex
     * @return true:If the exception information is processed, otherwise false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        new Thread(){
            @Override
            public void run() {
                Looper.prepare();
                /*Toast.makeText(application.getApplicationContext(), "APP Error",
                        Toast.LENGTH_SHORT).show();*/
                Looper.loop();
            }
        }.start();
        return true;
    }
}
