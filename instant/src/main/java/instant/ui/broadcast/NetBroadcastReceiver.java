package instant.ui.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import instant.bean.Session;
import instant.bean.UserCookie;
import instant.parser.localreceiver.ConnectLocalReceiver;
import instant.ui.InstantSdk;
import instant.utils.TimeUtil;
import instant.utils.log.LogManager;

/**
 * Created by gtq on 2016/12/29.
 */
public class NetBroadcastReceiver extends BroadcastReceiver {

    private String Tag = "_NetBroadcastReceiver";
    /** The default for repeated connection broadcast time */
    private static final int TIME_REPEART = 5000;
    /** The last received time */
    private long lastReceiveTime = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (context == null) {
                context = InstantSdk.instantSdk.getBaseContext();
            }
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo.State wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
            NetworkInfo.State mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();

            if ((wifiState != null && NetworkInfo.State.CONNECTED == wifiState) ||
                    (mobileState != null && NetworkInfo.State.CONNECTED == mobileState)) {//Network connection is successful
                LogManager.getLogger().d(Tag, "NetBroadcastReceiver onReceive()...Switch to the network environment");

                if (isCanConnect()) {
                    if (TimeUtil.getCurrentTimeInLong() - lastReceiveTime > TIME_REPEART) {
                        lastReceiveTime = TimeUtil.getCurrentTimeInLong();
                        //PushMessage.pushMessage(ServiceAck.CONNECT_START, new byte[0], ByteBuffer.allocate(0));
                    }
                }
            } else {
                LogManager.getLogger().d(Tag, "NetBroadcastReceiver onReceive()...Network disconnection");
                ConnectLocalReceiver.receiver.disConnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isCanConnect() {
        UserCookie userCookie = Session.getInstance().getUserCookie(Session.CONNECT_USER);
        return userCookie == null;
    }
}
