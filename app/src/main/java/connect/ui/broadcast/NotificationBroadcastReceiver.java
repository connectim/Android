package connect.ui.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.Serializable;

import connect.ui.activity.chat.ChatActivity;
import connect.ui.activity.login.StartActivity;
import connect.ui.base.BaseApplication;
import connect.utils.log.LogManager;

/**
 * Click on the notification bar to determine whether the private key is available
 * Created by pujin on 2017/4/17.
 */
public class NotificationBroadcastReceiver extends BroadcastReceiver {

    private String Tag = "NotificationBroadcastReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BaseApplication.getInstance().isEmptyActivity()) {
            LogManager.getLogger().d(Tag, "TO START ACTIVITY");
            intent = new Intent(context, StartActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else {
            LogManager.getLogger().d(Tag, "TO CHAT ACTIVITY");
            Serializable talker = intent.getSerializableExtra("ROOM_TALKER");

            intent = new Intent(context, ChatActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("ROOM_TALKER", talker);
            context.startActivity(intent);
        }
    }
}
