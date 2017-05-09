package connect.ui.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import connect.im.bean.ConnectState;
import connect.im.model.ConnectManager;
import connect.utils.TimeUtil;
import connect.utils.log.LogManager;

/**
 * Created by gtq on 2016/12/29.
 */
public class NetBroadcastReceiver extends BroadcastReceiver {

    private String Tag = "NetBroadcastReceiver";
    /** The default for repeated connection broadcast time */
    private static final int TIME_REPEART = 5000;
    /** The last received time */
    private long lastReceiveTime = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        NetworkInfo.State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

        if ((wifiState != null && NetworkInfo.State.CONNECTED == wifiState) ||
                (mobileState != null && NetworkInfo.State.CONNECTED == mobileState)) {//Network connection is successful
            LogManager.getLogger().d(Tag, "NetBroadcastReceiver onReceive()...Switch to the network environment");

            if (ConnectManager.getInstance().isCanConnect()) {
                if (TimeUtil.getCurrentTimeInLong() - lastReceiveTime > TIME_REPEART) {
                    ConnectManager.getInstance().stopConnect();
                    ConnectManager.getInstance().connectServer();
                    lastReceiveTime = TimeUtil.getCurrentTimeInLong();
                }
            }
        } else {
            LogManager.getLogger().d(Tag, "NetBroadcastReceiver onReceive()...Network disconnection");
            ConnectState.getInstance().sendEvent(ConnectState.ConnectType.DISCONN);
        }
    }
}
