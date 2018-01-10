package connect.utils;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import connect.activity.base.BaseApplication;
import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.Talker;
import connect.activity.home.bean.ConversationAction;
import connect.activity.home.bean.HttpRecBean;
import connect.activity.set.bean.SystemSetBean;
import connect.broadcast.NotificationBroadcastReceiver;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionSettingHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ConversionSettingEntity;
import connect.database.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.utils.system.SystemUtil;
import protos.Connect;

/**
 * notify bar
 */
public class NotificationBar {

    public static NotificationBar notificationBar = getInstance();
    private static final int MSG_NOTICE = 120;
    private static final long MSG_DELAYMILLIS = 2000;
    private long TIME_SENDNOTIFY = 0;

    private synchronized static NotificationBar getInstance() {
        if (notificationBar == null) {
            notificationBar = new NotificationBar();
        }
        return notificationBar;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            TIME_SENDNOTIFY = TimeUtil.getCurrentTimeInLong();

            Bundle bundle = msg.getData();
            int type = bundle.getInt("TYPE");
            String identify = bundle.getString("KEY");
            String content = bundle.getString("CONTENT");
            showNotification(identify, type, content);

            ConversationAction.conversationAction.sendEvent(ConversationAction.ConverType.LOAD_MESSAGE);
        }
    };

    /*** Refresh a message time recently */
    public void noticeBarMsg(String identify, int chattype, String content) {
        mHandler.removeMessages(MSG_NOTICE);

        android.os.Message message = mHandler.obtainMessage(MSG_NOTICE);
        Bundle bundle = new Bundle();
        bundle.putString("KEY", identify);
        bundle.putInt("TYPE", chattype);
        bundle.putSerializable("CONTENT", content);
        message.setData(bundle);

        if (TimeUtil.getCurrentTimeInLong() - TIME_SENDNOTIFY < MSG_DELAYMILLIS) {
            mHandler.sendMessage(message);
        } else {
            mHandler.sendMessageDelayed(message, MSG_DELAYMILLIS);
        }
    }

    /**
     * message notice
     *
     * @param roomkey friend pubkey/group pubkey
     */
    private void showNotification(String roomkey, int type, String content) {
        String runAcy = ActivityUtil.getRunningActivityName();
        String chatname = ChatActivity.class.getName();

        ConversionSettingEntity setEntity = ConversionSettingHelper.getInstance().loadSetEntity(roomkey);
        int notice = (null == setEntity || null == setEntity.getDisturb()) ? 0 : setEntity.getDisturb();
        if (notice == 1) return;

        SystemSetBean systemSetBean = ParamManager.getInstance().getSystemSet();
        if (systemSetBean.isRing()) {
            HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.SOUNDPOOL, chatname.equals(runAcy) ? 1 : 0);
        }
        if (systemSetBean.isVibrate()) {
            HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.SYSTEM_VIBRATION);
        }

        if (SystemUtil.isRunBackGround()) {
            notiticationBar(roomkey, type, content);
        } else {
            if (!chatname.equals(runAcy)) {
                notiticationBar(roomkey, type, content);
            }
        }
    }

    /**
     * Notification bar display
     *
     * @param roomid
     * @param type
     * @param content
     */
    private void notiticationBar(String roomid, int type, String content) {
        Context context = BaseApplication.getInstance();
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        Intent intent = new Intent();

        String tickerTitle = "Connnect";
        Talker talker = null;

        Connect.ChatType chatType = Connect.ChatType.forNumber(type);
        switch (chatType) {
            case PRIVATE:
                ContactEntity friend = ContactHelper.getInstance().loadFriendEntity(roomid);
                if (friend != null) {
                    talker = new Talker(Connect.ChatType.PRIVATE,roomid);
                    tickerTitle = TextUtils.isEmpty(friend.getRemark()) ? friend.getName() : friend.getRemark();
                }
                break;
            case GROUPCHAT:
                GroupEntity group = ContactHelper.getInstance().loadGroupEntity(roomid);
                if (group != null) {
                    talker = new Talker(Connect.ChatType.GROUPCHAT,roomid);
                    tickerTitle = group.getName();
                }
                break;
            case CONNECT_SYSTEM:
                talker = new Talker(Connect.ChatType.CONNECT_SYSTEM, BaseApplication.getInstance().getString(R.string.app_name));
                break;
        }

        intent.setClass(context, NotificationBroadcastReceiver.class);
        intent.setAction("com.notification");
        intent.putExtra("ROOM_TALKER", talker);

        content = GlobalLanguageUtil.getInstance().translateValue(content);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setContentTitle(tickerTitle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setContentText(content);
            // mBuilder..setFullScreenIntent(pendingIntent, true);//
            mBuilder.setCategory(NotificationCompat.CATEGORY_MESSAGE);
            mBuilder.setSmallIcon(R.mipmap.connect_logo);
        } else {
            mBuilder.setSmallIcon(R.mipmap.connect_logo);
            mBuilder.setContentText(content);
        }
        mBuilder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.connect_logo));
        mBuilder.setTicker(tickerTitle);
        mBuilder.setWhen(System.currentTimeMillis());
        mBuilder.setPriority(NotificationCompat.PRIORITY_MAX);
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        mBuilder.setAutoCancel(true);
        mBuilder.setOngoing(false);
        mBuilder.setDefaults(NotificationCompat.DEFAULT_LIGHTS);
        android.app.NotificationManager mNotificationManager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = mBuilder.build();//API 16
        mNotificationManager.notify(1001, notification);
    }
}
