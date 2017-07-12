package connect.activity.login;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import connect.activity.base.BaseActivity;

/**
 * Monitor the phone lock screen to unlock the event, when the screen lock screen to start the 1 pixel Activity,
 * the user will be destroyed when unlocking Activity. Note that the Activity needs to be designed to be user aware.
 * Created by pujin on 2017/5/11.
 */

public class KeepLiveActivity extends BaseActivity {
    private final static String Tag = "KeepLive";

    public KeepLiveActivity activity;
    private BroadcastReceiver receiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    public static void startActity(Context context) {
        Intent intent = new Intent(context, KeepLiveActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void initView() {
        activity = this;

        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.width = 1;
        params.height = 1;
        window.setAttributes(params);

        receiver =new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("KeepLive");
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkScreenOn();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkScreenOn() {
        PowerManager manager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = manager.isScreenOn();
        if (isScreenOn) {
            finish();
        }
    }
}
