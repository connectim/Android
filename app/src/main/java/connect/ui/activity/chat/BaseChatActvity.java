package connect.ui.activity.chat;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.ImageView;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.ConversionHelper;
import connect.db.green.DaoHelper.ConversionSettingHelper;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.DaoHelper.TransactionHelper;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.ConversionEntity;
import connect.im.bean.MsgType;
import connect.im.bean.UserOrderBean;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.BurnNotice;
import connect.ui.activity.chat.bean.ExtBean;
import connect.ui.activity.chat.bean.GatherBean;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.bean.MsgDirect;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.bean.MsgSend;
import connect.ui.activity.chat.bean.RecExtBean;
import connect.ui.activity.chat.bean.RoomSession;
import connect.ui.activity.chat.bean.Talker;
import connect.ui.activity.chat.bean.WebsiteExt1Bean;
import connect.ui.activity.chat.exts.GatherActivity;
import connect.ui.activity.chat.exts.RedPacketActivity;
import connect.ui.activity.chat.exts.TransferToActivity;
import connect.ui.activity.chat.inter.FileUpLoad;
import connect.ui.activity.chat.model.InputPanel;
import connect.ui.activity.chat.model.content.BaseChat;
import connect.ui.activity.chat.model.content.FriendChat;
import connect.ui.activity.chat.model.content.GroupChat;
import connect.ui.activity.chat.model.content.RobotChat;
import connect.ui.activity.chat.model.fileload.PhotoUpload;
import connect.ui.activity.chat.model.fileload.VideoUpload;
import connect.ui.activity.chat.model.fileload.VoiceUpload;
import connect.ui.activity.chat.set.ContactCardActivity;
import connect.ui.activity.contact.bean.ContactNotice;
import connect.ui.activity.home.HomeActivity;
import connect.ui.activity.locmap.GoogleMapActivity;
import connect.ui.activity.locmap.bean.GeoAddressBean;
import connect.ui.activity.wallet.TransferFriendSeleActivity;
import connect.ui.adapter.ChatAdapter;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import connect.utils.permission.PermissionUtil;
import connect.view.DialogView;
import connect.view.RecycleViewScrollHelper;
import connect.view.album.ui.activity.PhotoAlbumActivity;
import connect.view.camera.CameraTakeActivity;
import connect.view.imagewatcher.ImageWatcher;
import connect.view.imagewatcher.ImageWatcherUtil;
import connect.view.imgviewer.ImageViewerActivity;

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
    protected BaseChat baseChat;
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
        NotificationManager mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE) ;
        mNotificationManager.cancel(1001);

        talker = (Talker) getIntent().getSerializableExtra(ROOM_TALKER);
        roomSession = RoomSession.getInstance();
        roomSession.setRoomType(talker.getTalkType());
        roomSession.setRoomKey(talker.getTalkKey());

        switch (talker.getTalkType()) {
            case 0:
                roomSession.setRoomName(talker.getTalkName());
                roomSession.setFriendAvatar(talker.getFriendEntity().getAvatar());
                baseChat = new FriendChat(talker.getFriendEntity());

                if (!TextUtils.isEmpty(baseChat.address())) {
                    UserOrderBean userOrderBean = new UserOrderBean();
                    userOrderBean.friendChatCookie(baseChat.roomKey());
                }
                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.BURNSTATE, roomSession.getBurntime() == 0 ? 0 : 1);
                break;
            case 1:
                roomSession.setRoomName(talker.getTalkName());
                roomSession.setGroupEcdh(talker.getGroupEntity().getEcdh_key());
                baseChat = new GroupChat(talker.getGroupEntity());
                break;
            case 2:
                baseChat = RobotChat.getInstance();
                roomSession.setRoomName(baseChat.nickName());
                break;
        }

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
        inputPanel = new InputPanel(getWindow().getDecorView().findViewById(android.R.id.content));
        inputPanel.isGroupAt(talker.getTalkType() == 1);
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
     * @param msgSend
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onEventMainThread(MsgSend msgSend) {
        MsgDefinBean contentBean;
        String filePath = null;
        MsgEntity bean = null;
        FileUpLoad upLoad = null;

        Object[] objects = null;
        if (msgSend.getObj() != null) {
            objects = (Object[]) msgSend.getObj();
        }

        switch (msgSend.getMsgType()) {
            case Text:
                bean = (MsgEntity) baseChat.txtMsg((String) objects[0]);
                if (objects.length == 2) {
                    if (((List<String>) objects[1]).size() > 0) {
                        bean.getMsgDefinBean().setExt1(new Gson().toJson(objects[1]));
                    }
                }
                sendNormalMsg(true,bean);
                break;
            case Photo:
                List<String> paths = (List<String>) objects[0];
                for (String str : paths) {
                    bean = (MsgEntity) baseChat.photoMsg(str, FileUtil.fileSize(str));
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    options.inSampleSize = 1;
                    BitmapFactory.decodeFile(str, options);
                    contentBean = bean.getMsgDefinBean();
                    contentBean.setImageOriginWidth(options.outWidth);
                    contentBean.setImageOriginHeight(options.outHeight);

                    adapterInsetItem(bean);
                    upLoad = new PhotoUpload(activity, baseChat, contentBean, new FileUpLoad.FileUpListener() {
                        @Override
                        public void upSuccess(String msgid) {
                        }
                    });
                    upLoad.fileHandle();
                }
                break;
            case Video:
                filePath = (String) objects[0];
                bean = (MsgEntity) baseChat.videoMsg(filePath, (int) objects[1], FileUtil.fileSize(filePath));
                contentBean = bean.getMsgDefinBean();

                Bitmap thumbBitmap = BitmapUtil.thumbVideo(filePath);
                contentBean.setImageOriginWidth(thumbBitmap.getWidth());
                contentBean.setImageOriginHeight(thumbBitmap.getHeight());
                adapterInsetItem(bean);

                upLoad = new VideoUpload(activity, baseChat, contentBean, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }
                });
                upLoad.fileHandle();
                break;
            case Voice:
                bean = (MsgEntity) baseChat.voiceMsg((String) objects[0], (int) objects[1], FileUtil.fileSize(filePath));
                contentBean = bean.getMsgDefinBean();
                adapterInsetItem(bean);

                upLoad = new VoiceUpload(activity, baseChat, contentBean, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }
                });
                upLoad.fileHandle();
                break;
            case Emotion:
                bean = (MsgEntity) baseChat.emotionMsg((String) objects[0]);
                sendNormalMsg(true,bean);
                break;
            case Name_Card:
                bean = (MsgEntity) baseChat.cardMsg((ContactEntity) objects[0]);
                sendNormalMsg(true,bean);
                break;
            case Self_destruct_Notice:
                long time = (long) objects[0];
                RoomSession.getInstance().setBurntime(time);

                bean = (MsgEntity) baseChat.destructMsg((Long) objects[0]);
                sendNormalMsg(true,bean);
                break;
            case Self_destruct_Receipt:
                bean = (MsgEntity) baseChat.receiptMsg((String) objects[0]);
                baseChat.sendPushMsg(bean);
                break;
            case Request_Payment:
                GatherBean gatherBean = (GatherBean) objects[0];
                bean = (MsgEntity) baseChat.paymentMsg(gatherBean);
                sendNormalMsg(true,bean);

                //add payment information
                TransactionHelper.getInstance().updateTransEntity(gatherBean.getHashid(), bean.getMsgid(), 0, gatherBean.getTotalMember());
                break;
            case Transfer:
                bean = (MsgEntity) baseChat.transferMsg((String) objects[0], (long) objects[1], (String) objects[2], 0);
                sendNormalMsg(true,bean);

                //add payment information
                TransactionHelper.getInstance().updateTransEntity((String) objects[0], bean.getMsgid(), 1);
                break;
            case Location:
                GeoAddressBean geoAddress = (GeoAddressBean) objects[0];
                bean = (MsgEntity) baseChat.locationMsg(geoAddress.getPath(), geoAddress);
                contentBean = bean.getMsgDefinBean();
                adapterInsetItem(bean);

                upLoad = new PhotoUpload(activity, baseChat, contentBean, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }
                });
                upLoad.fileHandle();
                break;
            case Lucky_Packet:
                bean = (MsgEntity) baseChat.luckPacketMsg((String) objects[0], (String) objects[1], 0);
                sendNormalMsg(true,bean);
                break;
            case OUTER_WEBSITE:
                bean = (MsgEntity) baseChat.outerWebsiteMsg((String) objects[0], (WebsiteExt1Bean) objects[1]);
                sendNormalMsg(true,bean);
                break;
        }
    }

    /**
     * message receive
     *
     * @param bean
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onEventMainThread(RecExtBean bean) {
        MsgEntity msgEntity = null;

        Object[] objects = null;
        if (bean.getObj() != null) {
            objects = (Object[]) bean.getObj();
        }

        switch (bean.getExtType()) {
            case HIDEPANEL:
                inputPanel.hideBottomPanel();
                break;
            case DELMSG:
                chatAdapter.removeItem((MsgEntity) objects[0]);
                break;
            case RECENT_ALBUM://Picture taken recently
                PermissionUtil.getInstance().requestPermissom(activity,new String[]{PermissionUtil.PERMISSIM_STORAGE},permissomCallBack);
                break;
            case OPEN_ALBUM://Open the photo album
                PhotoAlbumActivity.startActivity(activity, PhotoAlbumActivity.OPEN_ALBUM_CODE);
                break;
            case TAKE_PHOTO:
                CameraTakeActivity.startActivity(activity, CODE_TAKEPHOTO);
                break;
            case CLEAR_HISTORY:
                chatAdapter.clearHistory();
                MessageHelper.getInstance().deleteRoomMsg(talker.getTalkKey());
                break;
            case TRANSFER:
                if (baseChat.roomType() == 0) {
                    TransferToActivity.startActivity(activity, baseChat.address());
                } else if (RoomSession.getInstance().getRoomType() == 1) {
                    TransferFriendSeleActivity.startActivity(activity, 0, TransferFriendSeleActivity.SOURCE_GROUP, baseChat.roomKey());
                }
                break;
            case REDPACKET:
                RedPacketActivity.startActivity(activity, talker.getTalkType(), talker.getTalkKey());
                break;
            case GATHER:
                GatherActivity.startActivity(activity, talker.getTalkType(), talker.getTalkKey());
                break;
            case NAMECARD:
                ContactCardActivity.startActivity(activity);
                break;
            case MSGSTATE://message send state 0:sending 1:send success 2:send fail 3:send refuse
                if (talker.getTalkKey().equals(objects[0])) {
                    String msgid = (String) objects[1];
                    int state = (int) objects[2];

                    chatAdapter.updateItemSendState(msgid, state);
                    RecExtBean.sendRecExtMsg(RecExtBean.ExtType.MSGSTATEVIEW, msgid, state);
                }
                break;
            case RESEND://resend message
                reSendFailMsg((MsgEntity) objects[0]);
                break;
            case IMGVIEWER://Image viewer
                ArrayList<String> imgs = chatAdapter.showImgMsgs();
                ImageViewerActivity.startActivity(activity, (String) objects[0], imgs);
                break;
            case NOTICE://notice message
                adapterInsetItem((MsgEntity) baseChat.noticeMsg((String) objects[0]));
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
                MsgEntity tempEntity = ((FriendChat) baseChat).strangerNotice();
                MessageHelper.getInstance().insertToMsg(tempEntity.getMsgDefinBean());
                adapterInsetItem(tempEntity);
                break;
            case BURNSTATE://burn message state
                updateBurnState((Integer) objects[0]);
                break;
            case BURNMSG_READ://burn message start read
                String burnid = (String) objects[0];
                MsgDirect direct = (MsgDirect) objects[1];
                chatAdapter.hasReadBurnMsg(burnid);
                BurnNotice.sendBurnMsg(BurnNotice.BurnType.BURN_READ, burnid);

                if (direct == MsgDirect.From) {
                    MsgSend.sendOuterMsg(MsgType.Self_destruct_Receipt, burnid);
                }
                break;
            case GROUP_UPDATENAME://update group name
                ((GroupChat) baseChat).setNickName((String) objects[0]);
                updateBurnState(RoomSession.getInstance().getBurntime() == 0 ? 0 : 1);
                break;
            case GROUP_UPDATEMYNAME://update my nick in group
                ((GroupChat) baseChat).updateMyNickName();
                break;
            case MAP_LOCATION:
                GoogleMapActivity.startActivity(activity);
                break;
            case LUCKPACKET_RECEIVE://receive a lucky packet
                msgEntity = (MsgEntity) baseChat.clickReceiveLuckMsg((String) objects[0]);
                sendNormalMsg(false, msgEntity);
                break;
            case GROUP_REMOVE://dissolute group
                if (baseChat.roomKey().equals(objects[0])) {
                    ((GroupChat) baseChat).setGroupEntity(null);
                    ActivityUtil.backActivityWithClearTop(activity, HomeActivity.class);
                }
                break;
            case UNARRIVE_UPDATE://update chat Cookie
                if (baseChat.roomKey().equals(objects[0])) {
                    ((FriendChat) baseChat).setFriendCookie(null);
                    ((FriendChat) baseChat).loadFriendCookie(baseChat.roomKey());
                }
                break;
            case UNARRIVE_HALF:
                if (baseChat.roomKey().equals(objects[0])) {
                    ((FriendChat) baseChat).setEncryType(FriendChat.EncryType.HALF);
                }
                break;
            case MESSAGE_RECEIVE:
                if (objects[0].equals(talker.getTalkKey())) {
                    msgEntity = (MsgEntity) objects[1];
                    msgEntity.setPubkey(talker.getTalkKey());

                    long time = 0;
                    String msgid = null;
                    ExtBean extBean = null;

                    MsgDefinBean msgbean = msgEntity.getMsgDefinBean();
                    switch (MsgType.toMsgType(msgbean.getType())) {
                        case Self_destruct_Notice://Accept each other send after reading
                            time = Long.parseLong(msgbean.getContent());
                            if (time != RoomSession.getInstance().getBurntime()) {
                                RoomSession.getInstance().setBurntime(time);
                                BurnNotice.sendBurnMsg(BurnNotice.BurnType.BURN_START, time);
                                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.BURNSTATE, time == 0 ? 0 : 1);
                            }
                            adapterInsetItem(msgEntity);
                            ConversionSettingHelper.getInstance().updateBurnTime(talker.getTalkKey(), time);
                            break;
                        case Self_destruct_Receipt://Accept each other has read one after reading
                            msgid = msgbean.getContent();
                            RecExtBean.sendRecExtMsg(RecExtBean.ExtType.BURNMSG_READ, msgid, MsgDirect.To);
                            break;
                        default:
                            adapterInsetItem(msgEntity);

                            if (talker.getTalkType() == 0 && msgEntity.getMsgDefinBean().getSenderInfoExt() != null) {
                                String friendName = talker.getFriendEntity().getUsername();
                                String friendAvatar = talker.getFriendEntity().getAvatar();
                                String sendName = msgEntity.getMsgDefinBean().getSenderInfoExt().username;
                                String sendAvatar = msgEntity.getMsgDefinBean().getSenderInfoExt().avatar;
                                if (!friendName.equals(sendName) || !friendAvatar.equals(sendAvatar)) {
                                    ContactEntity entity = ContactHelper.getInstance().loadFriendEntity(talker.getTalkKey());
                                    if (entity != null) {
                                        entity.setUsername(sendName);
                                        entity.setAvatar(sendAvatar);
                                        ContactHelper.getInstance().insertContact(entity);
                                        ContactNotice.receiverContact();

                                        ((FriendChat) baseChat).setFriendEntity(entity);
                                    }
                                }
                            }

                            if (!TextUtils.isEmpty(msgbean.getExt())) {
                                extBean = new Gson().fromJson(msgbean.getExt(), ExtBean.class);
                                if (extBean != null) {
                                    time = extBean.getLuck_delete();
                                }
                            }
                            if (time != RoomSession.getInstance().getBurntime()) {
                                RoomSession.getInstance().setBurntime(time);
                                BurnNotice.sendBurnMsg(BurnNotice.BurnType.BURN_START, time);
                                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.BURNSTATE, time == 0 ? 0 : 1);
                                ConversionSettingHelper.getInstance().updateBurnTime(talker.getTalkKey(), time);

                                MsgEntity destructEntity = (MsgEntity) baseChat.destructMsg(time);
                                destructEntity.getMsgDefinBean().setSenderInfoExt(msgbean.getSenderInfoExt());
                                sendNormalMsg(true,destructEntity);
                            }
                            break;
                    }
                }
                break;
        }
    }

    protected void reSendFailMsg(MsgEntity msg) {
        FileUpLoad upLoad = null;
        MsgDefinBean definBean = msg.getMsgDefinBean();
        switch (definBean.getType()) {
            case 2://voice message
                upLoad = new VoiceUpload(activity, baseChat, definBean, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }
                });
                upLoad.fileHandle();
                break;
            case 3://picture message
                upLoad = new PhotoUpload(activity, baseChat, definBean, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }
                });
                upLoad.fileHandle();
                break;
            case 4://video message
                upLoad = new VideoUpload(activity, baseChat, definBean, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }
                });
                upLoad.fileHandle();
                break;
            case 17://location message
                upLoad = new PhotoUpload(activity, baseChat, definBean, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }
                });
                upLoad.fileHandle();
                break;
            default:
                baseChat.sendPushMsg(msg);
                break;
        }
    }

    public void sendNormalMsg(boolean needSend, MsgEntity bean) {
        MessageHelper.getInstance().insertToMsg(bean.getMsgDefinBean());
        adapterInsetItem(bean);
        if (needSend) {
            baseChat.sendPushMsg(bean);
        }
    }

    protected boolean isOpenRecord = false;
    protected PermissionUtil.ResultCallBack permissomCallBack = new PermissionUtil.ResultCallBack(){
        @Override
        public void granted(String[] permissions) {
            if(permissions != null || permissions.length > 0){
                if(permissions[0].equals(PermissionUtil.PERMISSIM_RECORD_AUDIO)){
                    isOpenRecord = true;
                }else if(permissions[0].equals(PermissionUtil.PERMISSIM_STORAGE)){
                    DialogView dialogView = new DialogView();
                    dialogView.showPhotoPick(activity);
                }
            }
        }

        @Override
        public void deny(String[] permissions) {
            if(permissions != null || permissions.length > 0){
                if(permissions[0].equals(PermissionUtil.PERMISSIM_RECORD_AUDIO)){
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
    public abstract void adapterInsetItem(MsgEntity bean);

    /** update title bar */
    public abstract void updateBurnState(int state);

    public BaseChat getBaseChat() {
        return baseChat;
    }

    public boolean isOpenRecord() {
        return isOpenRecord;
    }
}
