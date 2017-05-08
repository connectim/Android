package connect.ui.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import connect.im.model.ConnectManager;
import connect.ui.activity.R;
import connect.ui.activity.home.HomeActivity;

/**
 * Socket long service
 * Created by gtq on 2016/11/21.
 */
public class SocketService extends Service {

    private final static int FOREGROUND_ID = 1000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void startService(Context context) {
        Intent intent = new Intent(context, SocketService.class);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ConnectManager.getInstance().connectServer();
        //forgroundNotify();
        return super.onStartCommand(intent, flags, startId);
    }

    public static void stopServer(Context context) {
        Intent intent = new Intent(context, SocketService.class);
        context.stopService(intent);
    }

    public void forgroundNotify() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.mipmap.connect_logo);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText("");
        builder.setContentInfo(getString(R.string.app_name));
        builder.setWhen(System.currentTimeMillis());
        Intent activityIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        startForeground(FOREGROUND_ID, notification);
    }
}
