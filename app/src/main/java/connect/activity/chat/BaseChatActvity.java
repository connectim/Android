package connect.activity.chat;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.text.TextUtils;
import android.view.KeyEvent;
import com.google.protobuf.InvalidProtocolBufferException;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import connect.activity.base.BaseActivity;
import connect.activity.chat.adapter.ChatAdapter;
import connect.activity.base.BaseListener;
import connect.activity.chat.bean.DestructOpenBean;
import connect.activity.chat.bean.DestructReadBean;
import connect.activity.chat.bean.GeoAddressBean;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.bean.RoomSession;
import connect.activity.chat.bean.Talker;
import connect.activity.chat.exts.GoogleMapActivity;
import connect.activity.chat.exts.LuckyPacketActivity;
import connect.activity.chat.exts.PaymentActivity;
import connect.activity.chat.exts.TransferToActivity;
import connect.activity.chat.inter.FileUpLoad;
import connect.activity.chat.model.InputPanel;
import connect.activity.chat.model.fileload.LocationUpload;
import connect.activity.chat.model.fileload.PhotoUpload;
import connect.activity.chat.model.fileload.VideoUpload;
import connect.activity.chat.model.fileload.VoiceUpload;
import connect.activity.chat.set.ContactCardActivity;
import connect.activity.common.selefriend.SeleUsersActivity;
import connect.activity.home.HomeActivity;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.ConversionSettingHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.database.green.bean.ConversionEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import connect.utils.permission.PermissionUtil;
import connect.widget.DialogView;
import connect.widget.RecycleViewScrollHelper;
import connect.widget.album.AlbumActivity;
import connect.widget.camera.CameraTakeActivity;
import connect.widget.imagewatcher.ImageViewerActivity;
import connect.widget.imagewatcher.ImageWatcher;
import connect.widget.imagewatcher.ImageWatcherUtil;
import instant.bean.ChatMsgEntity;
import instant.bean.MessageType;
import instant.sender.model.BaseChat;
import instant.sender.model.FriendChat;
import instant.sender.model.GroupChat;
import instant.sender.model.NormalChat;
import instant.sender.model.RobotChat;
import protos.Connect;

/**
 * Created by pujin on 2017/1/19.
 */
public abstract class BaseChatActvity extends BaseActivity {

    protected static String ROOM_TALKER = "ROOM_TALKER";
    protected BaseChatActvity activity;

    /** take photo */
    protected final int CODE_TAKEPHOTO = 150;
    protected static final int CODE_REQUEST = 512;

    protected Talker talker;
    protected RoomSession roomSession;
    protected ConversionEntity roomEntity;
    protected NormalChat normalChat;
    protected ChatAdapter chatAdapter;
    protected RecycleViewScrollHelper scrollHelper;
    protected InputPanel inputPanel = null;
    protected LinearLayoutManager linearLayoutManager;
    protected ImageWatcher vImageWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void initView() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1001);

        talker = (Talker) getIntent().getSerializableExtra(ROOM_TALKER);
        roomSession = RoomSession.getInstance();
        roomSession.setRoomType(talker.getTalkType());
        roomSession.setRoomKey(talker.getTalkKey());

        Connect.ChatType chatType = talker.getTalkType();
        switch (chatType) {
            case PRIVATE:
                normalChat = new FriendChat(talker.getTalkKey());
                break;
            case GROUPCHAT:
                normalChat = new GroupChat(talker.getTalkKey());
                break;
            case CONNECT_SYSTEM:
                normalChat = RobotChat.getInstance();
                break;
        }

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        inputPanel = new InputPanel(getWindow().getDecorView().findViewById(android.R.id.content));
        inputPanel.isGroupAt(talker.getTalkType() == Connect.ChatType.GROUPCHAT);
        scrollHelper = new RecycleViewScrollHelper(new RecycleViewScrollHelper.OnScrollPositionChangedListener() {

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
        });
        scrollHelper.setCheckIfItemViewFullRecycleViewForTop(true);

        roomEntity = ConversionHelper.getInstance().loadRoomEnitity(talker.getTalkKey());
        if (roomEntity != null) {
            if (!TextUtils.isEmpty(roomEntity.getDraft())) {
                inputPanel.insertDraft(" " + roomEntity.getDraft());
            }
            ConversionHelper.getInstance().updateRoomEntity(roomEntity);
        }

        vImageWatcher = ImageWatcher.Helper.with(this)
                .setTranslucentStatus(ImageWatcherUtil.isShowBarHeight(this))
                .setErrorImageRes(R.mipmap.img_default)
                .create();
    }

    /**
     * mssage send
     *
     * @param msgSend
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onEventMainThread(MsgSend msgSend) {
        String filePath = null;
        ChatMsgEntity chatMsgEntity = null;
        FileUpLoad upLoad = null;

        Object[] objects = null;
        if (msgSend.getObj() != null) {
            objects = (Object[]) msgSend.getObj();
        }

        switch (msgSend.getMsgType()) {
            case Text:
                String txt = (String) objects[0];
                if (normalChat.chatType() == Connect.ChatType.GROUPCHAT_VALUE) {
                    List<String> atList = (List<String>) objects[1];
                    chatMsgEntity = ((GroupChat) normalChat).groupTxtMsg(txt, atList);
                } else {
                    chatMsgEntity = normalChat.txtMsg(txt);
                }
                sendNormalMsg(true, chatMsgEntity);
                break;
            case Photo:
                List<String> paths = (List<String>) objects[0];
                for (String str : paths) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    options.inSampleSize = 1;
                    BitmapFactory.decodeFile(str, options);

                    chatMsgEntity = normalChat.photoMsg(str, str, FileUtil.fileSize(str), options.outWidth, options.outHeight);

                    adapterInsetItem(chatMsgEntity);
                    upLoad = new PhotoUpload(activity, normalChat, chatMsgEntity, new FileUpLoad.FileUpListener() {
                        @Override
                        public void upSuccess(String msgid) {
                        }
                    });
                    upLoad.fileHandle();
                }
                break;
            case Voice:
                chatMsgEntity = normalChat.voiceMsg((String) objects[0], (Integer) objects[1]);

                adapterInsetItem(chatMsgEntity);
                upLoad = new VoiceUpload(activity, normalChat, chatMsgEntity, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }
                });
                upLoad.fileHandle();
                break;
            case Emotion:
                chatMsgEntity = normalChat.emotionMsg((String) objects[0]);
                sendNormalMsg(true, chatMsgEntity);
                break;
            case Video:
                filePath = (String) objects[0];
                Bitmap thumbBitmap = BitmapUtil.thumbVideo(filePath);
                File thumbFile = BitmapUtil.getInstance().bitmapSavePath(thumbBitmap);
                chatMsgEntity = normalChat.videoMsg(thumbFile.getAbsolutePath(), filePath, (Integer) objects[1],
                        FileUtil.fileSizeOf(filePath), thumbBitmap.getWidth(), thumbBitmap.getHeight());

                adapterInsetItem(chatMsgEntity);
                upLoad = new VideoUpload(activity, normalChat, chatMsgEntity, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }
                });
                upLoad.fileHandle();
                break;
            case Name_Card:
                chatMsgEntity = normalChat.cardMsg((String) objects[0], (String) objects[1], (String) objects[2]);
                sendNormalMsg(true, chatMsgEntity);
                break;
            case Self_destruct_Notice:
                int time = (int) objects[0];
                RoomSession.getInstance().setBurntime(time);
                ConversionSettingHelper.getInstance().updateBurnTime(talker.getTalkKey(), time);

                chatMsgEntity = normalChat.destructMsg(time);
                sendNormalMsg(true, chatMsgEntity);
                break;
            case Self_destruct_Receipt:
                chatMsgEntity = normalChat.receiptMsg((String) objects[0]);
                normalChat.sendPushMsg(chatMsgEntity);
                break;
            case Request_Payment:
                int payType = (int) objects[0];
                String payHashId = (String) objects[1];
                long payAmount = (long) objects[2];
                int payMembers = (int) objects[3];
                String payTips = (String) objects[4];

                chatMsgEntity = normalChat.paymentMsg(payType, payHashId, payAmount, payMembers, payTips);
                sendNormalMsg(true, chatMsgEntity);
                //add payment information
                TransactionHelper.getInstance().updateTransEntity(payHashId, chatMsgEntity.getMessage_id(), 0, payMembers);
                break;
            case Transfer:
                int transferType = (int) objects[0];
                String transferHashId = (String) objects[1];
                long transferAmount = (long) objects[2];
                String transferTips = (String) objects[3];

                chatMsgEntity = normalChat.transferMsg(transferType, transferHashId, transferAmount, transferTips);
                sendNormalMsg(true, chatMsgEntity);

                //add payment information
                TransactionHelper.getInstance().updateTransEntity((String) objects[0], chatMsgEntity.getMessage_id(), 1);
                break;
            case Location:
                GeoAddressBean geoAddress = (GeoAddressBean) objects[0];

                chatMsgEntity = normalChat.locationMsg((float) geoAddress.getLocationLatitude(), (float) geoAddress.getLocationLongitude(),
                        geoAddress.getAddress(), geoAddress.getPath(), geoAddress.getImageOriginWidth(), geoAddress.getImageOriginHeight());

                adapterInsetItem(chatMsgEntity);
                upLoad = new LocationUpload(activity, normalChat, chatMsgEntity, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }
                });
                upLoad.fileHandle();
                break;
            case Lucky_Packet:
                int luckyType = (int) objects[0];
                String luckyHashId = (String) objects[1];
                String luckyTips = (String) objects[2];
                long luckyAmount = (long) objects[3];

                chatMsgEntity = normalChat.luckPacketMsg(luckyType, luckyHashId, luckyAmount,luckyTips);
                sendNormalMsg(true, chatMsgEntity);
                break;
            case OUTER_WEBSITE:
                String webUrl = (String) objects[0];
                String webTitle = (String) objects[1];
                String webSubtitle = (String) objects[2];
                String webImage = (String) objects[3];

                chatMsgEntity = normalChat.outerWebsiteMsg(webUrl, webTitle, webSubtitle, webImage);
                sendNormalMsg(true, chatMsgEntity);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DestructOpenBean openBean) {
        long time = openBean.getTime();
        updateBurnState(time);
    }

    /**
     * message receive
     *
     * @param bean
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onEventMainThread(RecExtBean bean) {
        ChatMsgEntity msgExtEntity = null;

        final Object[] objects;
        if (bean.getObj() == null) {
            objects = null;
        } else {
            objects = (Object[]) bean.getObj();
        }

        switch (bean.getExtType()) {
            case HIDEPANEL:
                inputPanel.hideBottomPanel();
                break;
            case DELMSG:
                chatAdapter.removeItem((ChatMsgEntity) objects[0]);
                break;
            case RECENT_ALBUM://Picture taken recently
                PermissionUtil.getInstance().requestPermission(activity, new String[]{PermissionUtil.PERMISSION_STORAGE}, permissomCallBack);
                break;
            case OPEN_ALBUM://Open the photo album
                AlbumActivity.startActivity(activity, AlbumActivity.OPEN_ALBUM_CODE);
                break;
            case TAKE_PHOTO:
                CameraTakeActivity.startActivity(activity, CODE_TAKEPHOTO);
                break;
            case CLEAR_HISTORY:
                chatAdapter.clearHistory();
                MessageHelper.getInstance().deleteRoomMsg(talker.getTalkKey());
                break;
            case TRANSFER:
                if (normalChat.chatType() == 0) {
                    TransferToActivity.startActivity(activity, normalChat.chatKey());
                } else if (normalChat.chatType() == 1) {
                    SeleUsersActivity.startActivity(activity, SeleUsersActivity.SOURCE_GROUP, talker.getTalkKey(), null);
                }
                break;
            case REDPACKET:
                int talkType = talker.getTalkType().getNumber();
                String talkKey = talker.getTalkKey();
                LuckyPacketActivity.startActivity(activity, talkType, talkKey);
                break;
            case PAYMENT:
                PaymentActivity.startActivity(activity, talker.getTalkType().getNumber(), talker.getTalkKey());
                break;
            case NAMECARD:
                ContactCardActivity.startActivity(activity);
                break;
            case MSGSTATE://message send state 0:sending 1:send success 2:send fail 3:send refuse
                if (talker.getTalkKey().equals(objects[0])) {
                    String msgid = (String) objects[1];
                    int state = (int) objects[2];

                    chatAdapter.updateItemSendState(msgid, state);
                    RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.MSGSTATEVIEW, msgid, state);
                }
                break;
            case RESEND://resend message
                reSendFailMsg((ChatMsgEntity) objects[0]);
                break;
            case IMGVIEWER://Image viewer
                chatAdapter.showImgMsgs(new BaseListener<ArrayList<String>>() {
                    @Override
                    public void Success(ArrayList<String> strings) {
                        ImageViewerActivity.startActivity(activity, (String) objects[0], strings);
                    }

                    @Override
                    public void fail(Object... objects) {

                    }
                });
                break;
            case NOTICE://notice message
                adapterInsetItem(normalChat.noticeMsg(0,(String) objects[0],""));
                break;
            case SCROLLBOTTOM:
                final int itemCounts = chatAdapter.getItemCount();
                scrollHelper.scrollToPosition(itemCounts);
                scrollHelper.setScrollBottom(true);
                break;
            case VOICE_UNREAD://Read the next unread voice message
                chatAdapter.unReadVoice((String) objects[0]);
                break;
            case NOTICE_NOTFRIEND://not friend
                if (normalChat.chatKey().equals(objects[0])) {
                    msgExtEntity = normalChat.noticeMsg(4, "", normalChat.chatKey());

                    MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                    adapterInsetItem(msgExtEntity);
                }
                break;
            case GROUP_UPDATENAME://update group name
                String identify = (String) objects[0];
                if (identify.equals(normalChat.chatKey())) {
                    String groupName = (String) objects[1];
                    ((GroupChat) normalChat).setNickName(groupName);
                    updateBurnState(RoomSession.getInstance().getBurntime() <= 0 ? 0 : 1);
                }
                break;
            case GROUP_UPDATEMYNAME://update my nick in group
                ((GroupChat) normalChat).updateMyNickName();
                break;
            case MAP_LOCATION:
                GoogleMapActivity.startActivity(activity);
                break;
            case LUCKPACKET_RECEIVE://receive a lucky packet
                msgExtEntity = normalChat.noticeMsg(3, (String) objects[0], (String) objects[1]);
                sendNormalMsg(false, msgExtEntity);
                break;
            case GROUP_REMOVE://dissolute group
                if (normalChat.chatKey().equals(objects[0])) {
                    ActivityUtil.backActivityWithClearTop(activity, HomeActivity.class);
                }
                break;
            case UNARRIVE_UPDATE://update chat Cookie
                if (normalChat.chatKey().equals(objects[0])) {
                    ((FriendChat) normalChat).setFriendCookie(null);
                    ((FriendChat) normalChat).loadFriendCookie(normalChat.chatKey());
                }
                break;
            case UNARRIVE_HALF:
                if (normalChat.chatKey().equals(objects[0])) {
                    ((FriendChat) normalChat).setEncryType(FriendChat.EncryType.HALF);
                }
                break;
            case MESSAGE_RECEIVE:
                if (objects[0].equals(talker.getTalkKey())) {
                    msgExtEntity = (ChatMsgEntity) objects[1];
                    int time = 0;
                    String msgid = null;

                    switch (MessageType.toMessageType(msgExtEntity.getMessageType())) {
                        case Self_destruct_Notice://open burn message notice
                            try {
                                Connect.DestructMessage destructMessage = Connect.DestructMessage.parseFrom(msgExtEntity.getContents());
                                time = destructMessage.getTime();
                                if (time != RoomSession.getInstance().getBurntime()) {
                                    RoomSession.getInstance().setBurntime(time);
                                    ConversionSettingHelper.getInstance().updateBurnTime(talker.getTalkKey(), time);

                                    DestructOpenBean.sendDestructMsg(time);
                                }
                                adapterInsetItem(msgExtEntity);
                                ConversionSettingHelper.getInstance().updateBurnTime(talker.getTalkKey(), time);
                            } catch (InvalidProtocolBufferException e) {
                                e.printStackTrace();
                            }
                            break;
                        case Self_destruct_Receipt://Accept each other has read one after reading
                            try {
                                Connect.ReadReceiptMessage readReceiptMessage = Connect.ReadReceiptMessage.parseFrom(msgExtEntity.getContents());

                                msgid = readReceiptMessage.getMessageId();
                                DestructReadBean.getInstance().sendEventDelay(msgid);
                            } catch (InvalidProtocolBufferException e) {
                                e.printStackTrace();
                            }
                            break;
                        default:
                            adapterInsetItem(msgExtEntity);

                            time = (int) msgExtEntity.parseDestructTime();
                            if (time != RoomSession.getInstance().getBurntime()) {
                                RoomSession.getInstance().setBurntime(time);
                                DestructOpenBean.sendDestructMsg(time);

                                ConversionSettingHelper.getInstance().updateBurnTime(talker.getTalkKey(), time);
                                msgExtEntity = normalChat.destructMsg(time);
                                sendNormalMsg(true, msgExtEntity);
                            }
                            break;
                    }
                }
                break;
        }
    }

    protected void reSendFailMsg(ChatMsgEntity msgExtEntity) {
        FileUpLoad upLoad = null;
        MessageType msgType = MessageType.toMessageType(msgExtEntity.getMessageType());
        switch (msgType) {
            case Photo://picture message
                upLoad = new PhotoUpload(activity, normalChat, msgExtEntity, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }
                });
                upLoad.fileHandle();
                break;
            case Voice://voice message
                upLoad = new VoiceUpload(activity, normalChat, msgExtEntity, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }
                });
                upLoad.fileHandle();
                break;
            case Video://video message
                upLoad = new VideoUpload(activity, normalChat, msgExtEntity, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }
                });
                upLoad.fileHandle();
                break;
            case Location://location message
                upLoad = new LocationUpload(activity, normalChat, msgExtEntity, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }
                });
                upLoad.fileHandle();
                break;
            default:
                normalChat.sendPushMsg(msgExtEntity);
                break;
        }
    }

    public void sendNormalMsg(boolean needSend, ChatMsgEntity bean) {
        MessageHelper.getInstance().insertMsgExtEntity(bean);
        adapterInsetItem(bean);
        if (needSend) {
            Message message = new Message();
            message.what = TAG_SEND;
            message.obj = bean;
            sHandler.sendMessageDelayed(message, 250);
        }
    }

    private int TAG_SEND = 150;
    private Handler sHandler = new Handler() {

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
    public abstract void updateBurnState(long time);

    public BaseChat getNormalChat() {
        return normalChat;
    }

    public boolean isOpenRecord() {
        return isOpenRecord;
    }
}
