package connect.ui.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import connect.im.model.ConnectManager;

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
        return super.onStartCommand(intent, flags, startId);
    }

    public static void stopServer(Context context) {
        Intent intent = new Intent(context, SocketService.class);
        context.stopService(intent);
    }
}
