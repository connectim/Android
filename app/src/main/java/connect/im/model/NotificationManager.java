package connect.im.model;

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
import connect.activity.home.bean.HttpRecBean;
import connect.activity.home.bean.MsgFragmReceiver;
import connect.broadcast.NotificationBroadcastReceiver;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionSettingHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ConversionSettingEntity;
import connect.database.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.GlobalLanguageUtil;
import connect.utils.TimeUtil;
import connect.utils.system.SystemUtil;
import protos.Connect;

/**
 * notify bar
 * Created by pujin on 2017/4/19.
 */

public class NotificationManager {

    private static NotificationManager manager;

    public static NotificationManager getInstance() {
        if (manager == null) {
            synchronized (NotificationManager.class) {
                if (manager == null) {
                    manager = new NotificationManager();
                }
            }
        }
        return manager;
    }

    private static final int MSG_NOTICE = 120;
    private static final long MSG_DELAYMILLIS = 2000;
    private long TIME_SENDNOTIFY = 0;

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
            MsgFragmReceiver.refreshRoom();
        }
    };

    /*** Refresh a message time recently */
    public void pushNoticeMsg(String identify, int chattype, String content) {
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
    public void showNotification(String roomkey, int type, String content) {
        String runAcy = ActivityUtil.getRunningActivityName();
        String chatname = ChatActivity.class.getName();

        ConversionSettingEntity setEntity = ConversionSettingHelper.getInstance().loadSetEntity(roomkey);
        int notice = (null == setEntity || null == setEntity.getDisturb()) ? 0 : setEntity.getDisturb();
        if (notice == 1) return;

        int voice = ParamManager.getInstance().getInt(ParamManager.SET_VOICE, 1);
        if (voice == 1) {
            HttpRecBean.sendHttpRecMsg(HttpRecBean.HttpRecType.SOUNDPOOL, chatname.equals(runAcy) ? 1 : 0);
        }
        int vibrate = ParamManager.getInstance().getInt(ParamManager.SET_VIBRATION, 1);
        if (vibrate == 1) {
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
        Talker talker=null;
        switch (type) {
            case 0:
                ContactEntity friend = ContactHelper.getInstance().loadFriendEntity(roomid);
                if (friend != null) {
                    talker = new Talker(friend);
                    tickerTitle= TextUtils.isEmpty(friend.getRemark())?friend.getUsername():friend.getRemark();
                }
                break;
            case 1:
                GroupEntity group = ContactHelper.getInstance().loadGroupEntity(roomid);
                if (group != null) {
                    talker = new Talker(group);
                    tickerTitle = group.getName();
                }
                break;
            case 2:
                talker = new Talker(Connect.ChatType.CONNECT_SYSTEM_VALUE, BaseApplication.getInstance().getString(R.string.app_name));
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
