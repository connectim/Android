package connect.ui.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import connect.ui.activity.login.KeepLiveActivity;

/**
 * Created by pujin on 2017/5/11.
 *
 * Monitor the phone lock screen to unlock the event, when the screen lock screen to start the 1 pixel Activity,
 * the user will be destroyed when unlocking Activity. Note that the Activity needs to be designed to be user aware.
 * Through this program, the priority of the process can be increased from 4 to the highest priority in the screen lock screen time by up to 1.
 */

public class KeepLiveReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            KeepLiveActivity.startActity(context);
        } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            KeepLiveActivity.stopActivity();
        }
    }
}
