package connect.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.Serializable;

import connect.activity.chat.ChatActivity;
import connect.activity.login.StartPageActivity;
import connect.activity.base.BaseApplication;
import connect.utils.log.LogManager;
import protos.Connect;

/**
 * Click on the notification bar to determine whether the private key is available
 * Created by pujin on 2017/4/17.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private static String TAG = "_NotificationBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BaseApplication.getInstance().isEmptyActivity()) {
            LogManager.getLogger().d(TAG, "TO START ACTIVITY");
            intent = new Intent(context, StartPageActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            LogManager.getLogger().d(TAG, "TO CHAT ACTIVITY");
            int chattype = intent.getIntExtra("ROOM_TYPE", 0);
            String identify = intent.getStringExtra("ROOM_IDENTIFY");

            intent = new Intent(context, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("CHAT_TYPE", Connect.ChatType.forNumber(chattype));
            intent.putExtra("CHAT_IDENTIFY", identify);
            context.startActivity(intent);
        }
    }
}
