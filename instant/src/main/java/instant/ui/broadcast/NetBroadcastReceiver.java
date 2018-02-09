package instant.ui.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import instant.parser.localreceiver.ConnectLocalReceiver;
import instant.ui.InstantSdk;
import instant.utils.TimeUtil;
import instant.utils.log.LogManager;

/**
 * Created by gtq on 2016/12/29.
 */
public class NetBroadcastReceiver extends BroadcastReceiver {

    private static String TAG = "_NetBroadcastReceiver";

    /** The default for repeated connection broadcast time */
    private static final int TIME_REPEART = 5000;
    /** The last received time */
    private long lastReceiveTime = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (context == null) {
                context = InstantSdk.getInstance().getBaseContext();
            }
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo.State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            NetworkInfo.State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

            if ((wifiState != null && NetworkInfo.State.CONNECTED == wifiState) ||
                    (mobileState != null && NetworkInfo.State.CONNECTED == mobileState)) {//Network connection is successful
                LogManager.getLogger().d(TAG, "NetBroadcastReceiver onReceive()...Switch to the network environment");

                if (TimeUtil.getCurrentTimeInLong() - lastReceiveTime > TIME_REPEART) {
                    lastReceiveTime = TimeUtil.getCurrentTimeInLong();
                }
            } else {
                LogManager.getLogger().d(TAG, "NetBroadcastReceiver onReceive()...Network disconnection");
                ConnectLocalReceiver.receiver.disConnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
