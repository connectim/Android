package connect.activity.chat.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;

import org.greenrobot.eventbus.EventBus;

import connect.activity.base.BaseActivity;
import connect.activity.base.BaseListener;
import connect.activity.chat.adapter.ChatAdapter;
import connect.activity.chat.bean.RoomSession;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ConversionEntity;
import connect.instant.model.CFriendChat;
import connect.instant.model.CGroupChat;
import connect.instant.model.CRobotChat;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.permission.PermissionUtil;
import connect.widget.DialogView;
import connect.widget.RecycleViewScrollHelper;
import connect.widget.bottominput.view.InputBottomLayout;
import connect.widget.imagewatcher.ImageWatcher;
import connect.widget.imagewatcher.ImageWatcherUtil;
import instant.bean.ChatMsgEntity;
import instant.sender.model.BaseChat;
import instant.sender.model.NormalChat;
import protos.Connect;

/**
 * Created by pujin on 2017/1/19.
 */
public abstract class BaseChatActvity extends BaseActivity {

    protected BaseChatActvity activity;

    protected static int CODE_TAKEPHOTO = 150;
    protected static final int CODE_REQUEST = 512;
    protected RoomSession roomSession;

    protected RecycleViewScrollHelper scrollHelper;

    protected NormalChat normalChat;
    protected ChatAdapter chatAdapter;
    protected ScrollPositionListener positionListener = new ScrollPositionListener();

    protected Connect.ChatType chatType;
    protected String chatIdentify;
    protected String searchTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void initView() {
        activity = this;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1001);

        roomSession = RoomSession.getInstance();
        roomSession.setRoomType(chatType);
        roomSession.setRoomKey(chatIdentify);

        switch (chatType) {
            case PRIVATE:
                CFriendChat cFriendChat = new CFriendChat(chatIdentify, new BaseListener<String>() {
                    @Override
                    public void Success(String ts) {
                        updateTitleName();
                    }

                    @Override
                    public void fail(Object... objects) {

                    }
                });
                normalChat = cFriendChat;
                break;
            case GROUPCHAT:
            case GROUP_DISCUSSION:
                normalChat = new CGroupChat(chatIdentify);
                updateTitleName();
                break;
            case CONNECT_SYSTEM:
                normalChat = CRobotChat.getInstance();
                updateTitleName();
                break;
        }

        scrollHelper = new RecycleViewScrollHelper(positionListener);

        ConversionEntity roomEntity = ConversionHelper.getInstance().loadRoomEnitity(chatIdentify);
        if (roomEntity != null) {
            if (!TextUtils.isEmpty(roomEntity.getDraft())) {
                InputBottomLayout.bottomLayout.insertDraft(" " + roomEntity.getDraft());
            }
            ConversionHelper.getInstance().updateRoomEntity(roomEntity);
        }

        ImageWatcher vImageWatcher = ImageWatcher.Helper.with(this)
                .setTranslucentStatus(ImageWatcherUtil.isShowBarHeight(this))
                .setErrorImageRes(R.mipmap.img_default)
                .create();
    }

    public void sendNormalMsg(boolean needSend, ChatMsgEntity bean) {
        MessageHelper.getInstance().insertMsgExtEntity(bean);
        adapterInsetItem(bean);
        if (needSend) {
            Message message = new Message();
            message.what = TAG_SEND;
            message.obj = bean;
            sendHandler.sendMessageDelayed(message, 250);
        }
    }

    private int TAG_SEND = 150;
    private Handler sendHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 150:
                    ChatMsgEntity msgExtEntity = (ChatMsgEntity) msg.obj;
                    normalChat.sendPushMsg(msgExtEntity);
                    break;
            }
        }
    };

    protected boolean isOpenRecord = false;
    protected PermissionUtil.ResultCallBack permissomCallBack = new PermissionUtil.ResultCallBack() {
        @Override
        public void granted(String[] permissions) {
            if (permissions != null || permissions.length > 0) {
                if (permissions[0].equals(PermissionUtil.PERMISSION_RECORD_AUDIO)) {
                    isOpenRecord = true;
                } else if (permissions[0].equals(PermissionUtil.PERMISSION_STORAGE)) {
                    DialogView dialogView = new DialogView();
                    dialogView.showPhotoPick(activity);
                }
            }
        }

        @Override
        public void deny(String[] permissions) {
            if (permissions != null || permissions.length > 0) {
                if (permissions[0].equals(PermissionUtil.PERMISSION_RECORD_AUDIO)) {
                    isOpenRecord = false;
                }
            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            saveRoomInfo();
            ActivityUtil.goBack(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        saveRoomInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    /** first start ,load message */
    public abstract void loadChatInfor();

    /** load more */
    public abstract void loadMoreMsgs();

    /** update chat list */
    public abstract void saveRoomInfo();

    /** insert item into adapter */
    public abstract void adapterInsetItem(ChatMsgEntity bean);

    /** update title bar */
    public abstract void updateTitleName();

    public BaseChat getNormalChat() {
        return normalChat;
    }

    public boolean isOpenRecord() {
        return isOpenRecord;
    }


    private class ScrollPositionListener implements RecycleViewScrollHelper.OnScrollPositionChangedListener {


        @Override
        public void onScrollToTop() {
            loadMoreMsgs();
        }

        @Override
        public void onScrollToBottom() {

        }

        @Override
        public void onScrollToUnknown(boolean isTopViewVisible, boolean isBottomViewVisible) {

        }
    }
}
