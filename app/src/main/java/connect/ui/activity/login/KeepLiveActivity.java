package connect.ui.activity.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import org.greenrobot.eventbus.EventBus;

import connect.ui.base.BaseActivity;

/**
 * Monitor the phone lock screen to unlock the event, when the screen lock screen to start the 1 pixel Activity,
 * the user will be destroyed when unlocking Activity. Note that the Activity needs to be designed to be user aware.
 * Created by pujin on 2017/5/11.
 */

public class KeepLiveActivity extends BaseActivity {
    private final static String Tag = "KeepLive";

    public static KeepLiveActivity keepLiveActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    public static void startActity(Context context) {
        Intent intent = new Intent(context, KeepLiveActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    public static void stopActivity() {
        if (keepLiveActivity != null) {
            keepLiveActivity.finish();
        }
    }

    @Override
    public void initView() {
        keepLiveActivity = this;

        Window window = getWindow();
        window.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 0;
        params.width = 1;
        params.height = 1;
        window.setAttributes(params);
    }
}
