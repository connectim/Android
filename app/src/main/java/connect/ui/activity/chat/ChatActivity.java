package connect.ui.activity.chat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.ConversionSettingHelper;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.DaoHelper.TransactionHelper;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.ConversionSettingEntity;
import connect.db.green.bean.GroupMemberEntity;
import connect.db.green.bean.TransactionEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.BaseEntity;
import connect.ui.activity.chat.bean.BurnNotice;
import connect.ui.activity.chat.bean.ExtBean;
import connect.ui.activity.chat.bean.GatherBean;
import connect.ui.activity.chat.bean.MsgChatReceiver;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.bean.MsgDirect;
import connect.ui.activity.chat.bean.MsgSend;
import connect.ui.activity.chat.bean.RecExtBean;
import connect.ui.activity.chat.bean.RoomSession;
import connect.ui.activity.chat.bean.Talker;
import connect.ui.activity.chat.bean.WebsiteExt1Bean;
import connect.ui.activity.chat.exts.GatherActivity;
import connect.ui.activity.chat.exts.RedPacketActivity;
import connect.ui.activity.chat.exts.TransferToActivity;
import connect.ui.activity.chat.inter.FileUpLoad;
import connect.ui.activity.chat.model.ChatMsgUtil;
import connect.ui.activity.chat.model.TranspondUtil;
import connect.ui.activity.chat.model.content.FriendChat;
import connect.ui.activity.chat.model.content.GroupChat;
import connect.ui.activity.chat.model.fileload.PhotoUpload;
import connect.ui.activity.chat.model.fileload.VideoUpload;
import connect.ui.activity.chat.model.fileload.VoiceUpload;
import connect.ui.activity.chat.set.ContactCardActivity;
import connect.ui.activity.chat.set.GroupSetActivity;
import connect.ui.activity.chat.set.SingleSetActivity;
import connect.ui.activity.chat.view.ExBottomLayout;
import connect.ui.activity.contact.bean.ContactNotice;
import connect.ui.activity.home.HomeActivity;
import connect.ui.activity.locmap.GoogleMapActivity;
import connect.ui.activity.locmap.bean.GeoAddressBean;
import connect.view.DialogView;
import connect.ui.activity.wallet.TransferFriendSeleActivity;
import connect.ui.adapter.ChatAdapter;
import connect.utils.ActivityUtil;
import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import connect.utils.MediaUtil;
import connect.utils.log.LogManager;
import connect.utils.permission.PermissiomUtilNew;
import connect.utils.TimeUtil;
import connect.view.TopToolBar;
import connect.view.album.entity.ImageInfo;
import connect.view.album.ui.activity.PhotoAlbumActivity;
import connect.view.camera.CameraTakeActivity;
import connect.view.imgviewer.ImageViewerActivity;

/**
 * chat message
 * Created by gtq on 2016/11/22.
 */
public class ChatActivity extends BaseChatActvity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recycler_chat)
    RecyclerView recyclerChat;
    @Bind(R.id.layout_exbottom)
    ExBottomLayout layoutExbottom;

    private String Tag = "ChatActivity";
    private ChatActivity activity;

    /** take photo */
    private final int CODE_TAKEPHOTO = 150;
    private static final int CODE_REQUEST = 512;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        initView();
        EventBus.getDefault().register(this);
    }

    public static void startActivity(Activity activity, Talker talker) {
        RoomSession.getInstance().setRoomType(talker.getTalkType());
        RoomSession.getInstance().setRoomKey(talker.getTalkKey());

        Bundle bundle = new Bundle();
        bundle.putSerializable(ROOM_TALKER, talker);
        ActivityUtil.next(activity, ChatActivity.class, bundle);
    }

    @Override
    public void initView() {
        super.initView();
        activity = this;
        toolbar.setBlackStyle();
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbar.setRightListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (talker.getTalkType()) {
                    case 0:
                        SingleSetActivity.startActivity(activity, talker.getTalkKey());
                        break;
                    case 1:
                        GroupSetActivity.startActivity(activity, talker.getTalkKey());
                        break;
                }
            }
        });
        //robot/stranger donot show setting
        if (!(talker.getTalkType() == 2 || baseChat.isStranger())) {
            toolbar.setRightImg(R.mipmap.menu_white);
        }

        ConversionSettingEntity chatSetEntity = ConversionSettingHelper.getInstance().loadSetEntity(talker.getTalkKey());
        long burntime = (chatSetEntity == null || chatSetEntity.getSnap_time() == null) ? 0 : chatSetEntity.getSnap_time();
        roomSession.setBurntime(burntime);

        chatAdapter = new ChatAdapter(activity, recyclerChat, linearLayoutManager);
        recyclerChat.setLayoutManager(linearLayoutManager);
        recyclerChat.setAdapter(chatAdapter);
        recyclerChat.setItemAnimator(new DefaultItemAnimator());
        recyclerChat.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                inputPanel.hideBottomPanel();
                return false;
            }
        });

        scrollHelper.attachToRecycleView(recyclerChat);
        loadChatInfor();

        PermissiomUtilNew.getInstance().requestPermissom(activity,
                new String[]{PermissiomUtilNew.PERMISSIM_RECORD_AUDIO,PermissiomUtilNew.PERMISSIM_STORAGE},
                permissomCallBack);
    }

    public void loadChatInfor() {
        new AsyncTask<Void, Void, List<BaseEntity>>() {

            @Override
            protected List<BaseEntity> doInBackground(Void... params) {
                return baseChat.loadChatEntities();
            }

            @Override
            protected void onPostExecute(List<BaseEntity> entities) {
                super.onPostExecute(entities);
                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.BURNSTATE, roomSession.getBurntime() == 0 ? 0 : 1);

                chatAdapter.setDatas(entities);

                BaseEntity encryEntity = (BaseEntity) baseChat.encryptChatMsg();
                if (entities.size() < 20 && encryEntity != null) {
                    long sendtime = entities.size() == 0 ? TimeUtil.getCurrentTimeInLong() :
                            entities.get(0).getMsgDefinBean().getSendtime();
                    encryEntity.getMsgDefinBean().setSendtime(sendtime);
                    entities.add(0, encryEntity);
                }
                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.SCROLLBOTTOM);
            }
        }.execute();
    }

    protected void loadMoreMsgs() {
        LogManager.getLogger().d(Tag, "loadMoreMsgs()");
        new AsyncTask<Void, Void, List<BaseEntity>>() {
            @Override
            protected List<BaseEntity> doInBackground(Void... params) {
                BaseEntity baseEntity = chatAdapter.getMsgEntities().get(0);
                return baseChat.loadMoreEntities(baseEntity.getMsgDefinBean().getSendtime());
            }

            @Override
            protected void onPostExecute(List<BaseEntity> msgEntities) {
                super.onPostExecute(msgEntities);
                if (msgEntities.size() > 0) {
                    View firstChild = recyclerChat.getChildAt(0);
                    int top = firstChild.getTop();

                    chatAdapter.insertMoreItems(msgEntities);
                    scrollHelper.scrollToPosition(msgEntities.size(), top);//Some errors, top - SystemUtil.dipToPx(48)
                }
            }
        }.execute();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onEventMainThread(MsgSend msgSend) {
        MsgDefinBean contentBean;
        String filePath = null;
        BaseEntity bean = null;
        FileUpLoad upLoad = null;
        TransactionEntity transEntity = null;

        Object[] objects = null;
        if (msgSend.getObj() != null) {
            objects = (Object[]) msgSend.getObj();
        }

        switch (msgSend.getMsgType()) {
            case Text:
                bean = (BaseEntity) baseChat.txtMsg((String) objects[0]);
                if (objects.length == 2) {
                    if (((List<String>) objects[1]).size() > 0) {
                        bean.getMsgDefinBean().setExt1(new Gson().toJson(objects[1]));
                    }
                }
                sendNormalMsg(bean);
                break;
            case Photo:
                List<String> paths = (List<String>) objects[0];
                for (String str : paths) {
                    bean = (BaseEntity) baseChat.photoMsg(str, FileUtil.fileSize(str));
                    contentBean = bean.getMsgDefinBean();
                    adapterInsetItem(bean);

                    upLoad = new PhotoUpload(activity, baseChat, contentBean, new FileUpLoad.FileUpListener() {
                        @Override
                        public void upSuccess(Object... objs) {
                            BaseEntity index = (BaseEntity) baseChat.photoMsg((String) objs[1], (String) objs[3]);

                            index.getMsgDefinBean().setMessage_id((String) objs[0]);
                            index.getMsgDefinBean().setUrl((String) objs[2]);
                            index.getMsgDefinBean().setImageOriginWidth((Float) objs[4]);
                            index.getMsgDefinBean().setImageOriginHeight((Float) objs[5]);
                            baseChat.sendPushMsg(index);
                        }
                    });
                    upLoad.fileHandle();
                }
                break;
            case Video:
                filePath = (String) objects[0];
                bean = (BaseEntity) baseChat.videoMsg(filePath, (int) objects[1], FileUtil.fileSize(filePath));
                contentBean = bean.getMsgDefinBean();

                Bitmap thumbBitmap = BitmapUtil.thumbVideo(filePath);
                contentBean.setImageOriginWidth(thumbBitmap.getWidth());
                contentBean.setImageOriginHeight(thumbBitmap.getHeight());
                adapterInsetItem(bean);

                upLoad = new VideoUpload(activity, baseChat, contentBean, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(Object... objs) {
                        BaseEntity index = (BaseEntity) baseChat.videoMsg((String) objs[1], (int) objs[3], (String) objs[4]);

                        index.getMsgDefinBean().setMessage_id((String) objs[0]);
                        index.getMsgDefinBean().setUrl((String) objs[2]);
                        index.getMsgDefinBean().setImageOriginWidth((Float) objs[5]);
                        index.getMsgDefinBean().setImageOriginHeight((Float) objs[6]);
                        baseChat.sendPushMsg(index);
                    }
                });
                upLoad.fileHandle();
                break;
            case Voice:
                bean = (BaseEntity) baseChat.voiceMsg((String) objects[0], (int) objects[1], FileUtil.fileSize(filePath));
                contentBean = bean.getMsgDefinBean();
                adapterInsetItem(bean);

                upLoad = new VoiceUpload(activity, baseChat, contentBean, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(Object... objs) {
                        BaseEntity index = (BaseEntity) baseChat.voiceMsg((String) objs[1], (int) objs[2], (String) objs[3]);

                        index.getMsgDefinBean().setMessage_id((String) objs[0]);
                        baseChat.sendPushMsg(index);
                    }
                });
                upLoad.fileHandle();
                break;
            case Emotion:
                bean = (BaseEntity) baseChat.emotionMsg((String) objects[0]);
                sendNormalMsg(bean);
                break;
            case Name_Card:
                bean = (BaseEntity) baseChat.cardMsg((ContactEntity) objects[0]);
                sendNormalMsg(bean);
                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.SCROLLBOTTOM);
                break;
            case Self_destruct_Notice:
                long time = (long) objects[0];
                RoomSession.getInstance().setBurntime(time);

                bean = (BaseEntity) baseChat.destructMsg((Long) objects[0]);
                sendNormalMsg(bean);
                break;
            case Self_destruct_Receipt:
                bean = (BaseEntity) baseChat.receiptMsg((String) objects[0]);
                baseChat.sendPushMsg(bean);
                break;
            case Request_Payment:
                GatherBean gatherBean = (GatherBean) objects[0];
                bean = (BaseEntity) baseChat.paymentMsg(gatherBean);
                sendNormalMsg(bean);
                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.SCROLLBOTTOM);

                //add payment information
                TransactionHelper.getInstance().updateTransEntity(gatherBean.getHashid(), bean.getMsgid(), 0, gatherBean.getTotalMember());
                break;
            case Transfer:
                bean = (BaseEntity) baseChat.transferMsg((String) objects[0], (long) objects[1], (String) objects[2], 0);
                sendNormalMsg(bean);
                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.SCROLLBOTTOM);

                //add payment information
                TransactionHelper.getInstance().updateTransEntity((String) objects[0], bean.getMsgid(), 1);
                break;
            case Location:
                GeoAddressBean geoAddress = (GeoAddressBean) objects[0];
                bean = (BaseEntity) baseChat.locationMsg(geoAddress.getPath(), geoAddress);
                contentBean = bean.getMsgDefinBean();
                adapterInsetItem(bean);

                upLoad = new PhotoUpload(activity, baseChat, contentBean, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(Object... objs) {
                        BaseEntity index = (BaseEntity) baseChat.locationMsg((String) objs[1], (GeoAddressBean) objs[2]);
                        index.getMsgDefinBean().setMessage_id((String) objs[0]);
                        index.getMsgDefinBean().setImageOriginWidth((Float) objs[3]);
                        index.getMsgDefinBean().setImageOriginHeight((Float) objs[4]);

                        baseChat.sendPushMsg(index);
                    }
                });
                upLoad.fileHandle();
                break;
            case Lucky_Packet:
                bean = (BaseEntity) baseChat.luckPacketMsg((String) objects[0], (String) objects[1], 0);
                sendNormalMsg(bean);
                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.SCROLLBOTTOM);
                break;
            case OUTER_WEBSITE:
                bean = (BaseEntity) baseChat.outerWebsiteMsg((String) objects[0], (WebsiteExt1Bean) objects[1]);
                sendNormalMsg(bean);
                break;
        }
    }

    /**
     * receive message from others, function orders
     *
     * @param bean
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onEventMainThread(RecExtBean bean) {
        BaseEntity baseEntity = null;

        Object[] objects = null;
        if (bean.getObj() != null) {
            objects = (Object[]) bean.getObj();
        }

        switch (bean.getExtType()) {
            case HIDEPANEL:
                inputPanel.hideBottomPanel();
                break;
            case DELMSG:
                chatAdapter.removeItem((BaseEntity) objects[0]);
                break;
            case RECENT_ALBUM://Picture taken recently
                PermissiomUtilNew.getInstance().requestPermissom(activity,new String[]{PermissiomUtilNew.PERMISSIM_STORAGE},permissomCallBack);
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
                reSendFailMsg((BaseEntity) objects[0]);
                break;
            case IMGVIEWER://Image viewer
                ArrayList<String> imgs = chatAdapter.showImgMsgs();
                ImageViewerActivity.startActivity(activity, (String) objects[0], imgs);
                break;
            case NOTICE://notice message
                adapterInsetItem((BaseEntity) baseChat.noticeMsg((String) objects[0]));
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
                BaseEntity tempEntity = ((FriendChat) baseChat).strangerNotice();
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
                baseEntity = (BaseEntity) baseChat.clickReceiveLuckMsg((String) objects[0]);
                sendNormalMsg(false, baseEntity);
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
        }
    }

    protected void updateBurnState(int state) {
        String titleName = "";
        switch (state) {
            case 0://not start
                if (baseChat.roomType() == 0 || baseChat.roomType() == 2) {
                    toolbar.setTitle(baseChat.nickName());
                } else {
                    List<GroupMemberEntity> memEntities = ContactHelper.getInstance().loadGroupMemEntity(baseChat.roomKey());
                    titleName = baseChat.nickName();
                    if (titleName.length() > 10) {
                        titleName = titleName.substring(0, 8);
                        titleName += "...";
                    }
                    toolbar.setTitle(titleName+String.format(Locale.ENGLISH,"(%d)",memEntities.size()));
                }
                break;
            case 1://havae started
                String name = baseChat.nickName();
                StringBuffer indexName = new StringBuffer();
                indexName.append(name.charAt(0));
                if (name.length() > 2) {
                    for (int i = 1; i < name.length() - 1; i++) {
                        indexName.append("*");
                    }
                }
                indexName.append(name.charAt(name.length() - 1));
                if (baseChat.roomType() == 0 || baseChat.roomType() == 2) {
                    toolbar.setTitle(R.mipmap.message_privacy_grey2x, indexName.toString());
                } else {
                    List<GroupMemberEntity> memEntities = ContactHelper.getInstance().loadGroupMemEntity(baseChat.roomKey());
                    toolbar.setTitle(indexName + String.format(Locale.ENGLISH,"(%d)", memEntities.size()));
                }
                break;
        }
    }

    protected void reSendFailMsg(BaseEntity msg) {
        FileUpLoad upLoad = null;
        MsgDefinBean definBean = msg.getMsgDefinBean();
        switch (definBean.getType()) {
            case 2://voice message
                upLoad = new VoiceUpload(activity, baseChat, definBean, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(Object... objs) {
                        BaseEntity index = (BaseEntity) baseChat.voiceMsg((String) objs[1], (int) objs[2], (String) objs[3]);

                        index.getMsgDefinBean().setMessage_id((String) objs[0]);
                        baseChat.sendPushMsg(index);
                    }
                });
                upLoad.fileHandle();
                break;
            case 3://picture message
                upLoad = new PhotoUpload(activity, baseChat, definBean, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(Object... objs) {
                        BaseEntity index = (BaseEntity) baseChat.photoMsg((String) objs[1], (String) objs[3]);

                        index.getMsgDefinBean().setMessage_id((String) objs[0]);
                        index.getMsgDefinBean().setUrl((String) objs[2]);
                        index.getMsgDefinBean().setImageOriginWidth((Float) objs[4]);
                        index.getMsgDefinBean().setImageOriginHeight((Float) objs[5]);
                        baseChat.sendPushMsg(index);
                    }
                });
                upLoad.fileHandle();
                break;
            case 4://video message
                upLoad = new VideoUpload(activity, baseChat, definBean, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(Object... objs) {
                        BaseEntity index = (BaseEntity) baseChat.videoMsg((String) objs[1], (int) objs[3], (String) objs[4]);

                        index.getMsgDefinBean().setMessage_id((String) objs[0]);
                        index.getMsgDefinBean().setUrl((String) objs[2]);
                        index.getMsgDefinBean().setImageOriginWidth((Float) objs[5]);
                        index.getMsgDefinBean().setImageOriginHeight((Float) objs[6]);
                        baseChat.sendPushMsg(index);
                    }
                });
                upLoad.fileHandle();
                break;
            case 17://location message
                upLoad = new PhotoUpload(activity, baseChat, definBean, new FileUpLoad.FileUpListener() {
                    @Override
                    public void upSuccess(Object... objs) {
                        BaseEntity index = (BaseEntity) baseChat.locationMsg((String) objs[1], (GeoAddressBean) objs[2]);
                        index.getMsgDefinBean().setMessage_id((String) objs[0]);
                        index.getMsgDefinBean().setImageOriginWidth((Float) objs[3]);
                        index.getMsgDefinBean().setImageOriginHeight((Float) objs[4]);

                        baseChat.sendPushMsg(index);
                    }
                });
                upLoad.fileHandle();
                break;
            default:
                baseChat.sendPushMsg(msg);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MsgChatReceiver receiver) {
        if (receiver.getPubKey().equals(talker.getTalkKey())) {
            BaseEntity bean = receiver.getBean();
            bean.setPubkey(talker.getTalkKey());

            long time = 0;
            String msgid = null;
            ExtBean extBean = null;

            MsgDefinBean msgbean = bean.getMsgDefinBean();
            switch (MsgType.toMsgType(msgbean.getType())) {
                case Self_destruct_Notice://Accept each other send after reading
                    time = Long.parseLong(msgbean.getContent());
                    if (time != RoomSession.getInstance().getBurntime()) {
                        RoomSession.getInstance().setBurntime(time);
                        BurnNotice.sendBurnMsg(BurnNotice.BurnType.BURN_START, time);
                        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.BURNSTATE, time == 0 ? 0 : 1);
                    }
                    adapterInsetItem(bean);
                    ConversionSettingHelper.getInstance().updateBurnTime(talker.getTalkKey(), time);
                    break;
                case Self_destruct_Receipt://Accept each other has read one after reading
                    msgid = msgbean.getContent();
                    RecExtBean.sendRecExtMsg(RecExtBean.ExtType.BURNMSG_READ, msgid, MsgDirect.To);
                    break;
                default:
                    adapterInsetItem(bean);

                    if (talker.getTalkType() == 0 && bean.getMsgDefinBean().getSenderInfoExt() != null) {
                        String friendName = talker.getFriendEntity().getUsername();
                        String friendAvatar = talker.getFriendEntity().getAvatar();
                        String sendName = bean.getMsgDefinBean().getSenderInfoExt().username;
                        String sendAvatar = bean.getMsgDefinBean().getSenderInfoExt().avatar;
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

                        BaseEntity destructEntity = (BaseEntity) baseChat.destructMsg(time);
                        destructEntity.getMsgDefinBean().setSenderInfoExt(msgbean.getSenderInfoExt());
                        sendNormalMsg(destructEntity);
                    }
                    break;
            }
        }
    }

    /**
     * Accept new message to slide to the end
     *
     * @param bean
     */
    protected void adapterInsetItem(BaseEntity bean) {
        chatAdapter.insertItem(bean);
        if (scrollHelper.isScrollBottom() || !talker.getTalkKey().equals(bean.getMsgDefinBean().getPublicKey())) {
            RecExtBean.sendRecExtMsg(RecExtBean.ExtType.SCROLLBOTTOM);
        }
    }

    protected void sendNormalMsg(BaseEntity bean) {
        sendNormalMsg(true,bean);
    }

    protected void sendNormalMsg(boolean needSend, BaseEntity bean) {
        MessageHelper.getInstance().insertToMsg(bean.getMsgDefinBean());
        adapterInsetItem(bean);
        if (needSend) {
            baseChat.sendPushMsg(bean);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CODE_TAKEPHOTO && data != null) {
            String type = data.getStringExtra("mediaType");
            String path = data.getStringExtra("path");

            if (TextUtils.isEmpty(type) || TextUtils.isEmpty(path)) {
                return;
            }

            if (type.equals(CameraTakeActivity.MEDIA_TYPE_PHOTO)) {
                List<String> paths = new ArrayList<>();
                paths.add(path);
                MsgSend.sendOuterMsg(MsgType.Photo, paths);
            } else if (type.equals(CameraTakeActivity.MEDIA_TYPE_VEDIO)) {
                int length = data.getIntExtra("length", 10);
                MsgSend.sendOuterMsg(MsgType.Video, path, length);
            }
        } else if (requestCode == PhotoAlbumActivity.OPEN_ALBUM_CODE && data != null) {
            List<ImageInfo> imageInfos = (List<ImageInfo>) data.getSerializableExtra("list");
            if (imageInfos != null && imageInfos.size() > 0) {
                for (ImageInfo info : imageInfos) {
                    if (info.getFileType() == 0) {
                        List<String> paths = new ArrayList<>();
                        paths.add(info.getImageFile().getAbsolutePath());
                        MsgSend.sendOuterMsg(MsgType.Photo, paths);
                    } else {
                        int length = (int) (info.getImageFile().getVideoLength() / 1000);
                        MsgSend.sendOuterMsg(MsgType.Video, info.getImageFile().getAbsolutePath(), length);
                    }
                }
            }
        } else if (requestCode == CODE_REQUEST && resultCode == CODE_REQUEST) {//relay the message
            transpondTo(data);
        }
    }

    /**
     * relay the message
     *
     * @param data
     */
    protected void transpondTo(Intent data) {
        int roomType = data.getIntExtra("type", 0);
        String roomkey = data.getStringExtra("object");
        Serializable serializables = data.getSerializableExtra("Serializable");
        Object[] objects = (Object[]) serializables;

        TranspondUtil transpondUtil = new TranspondUtil(activity, roomType, roomkey, Integer.parseInt((String) objects[0]), (String) objects[1]);
        transpondUtil.transpondTo();
    }

    @Override
    protected void saveRoomInfo() {
        String showtxt = "";
        long sendtime = 0;
        String draft = inputPanel.getDraft();

        if (chatAdapter.getMsgEntities().size() != 0) {
            BaseEntity lastmsg = chatAdapter.getMsgEntities().get(chatAdapter.getItemCount() - 1);
            if (lastmsg != null) {
                showtxt = ChatMsgUtil.showContentTxt(talker.getTalkType(), lastmsg.getMsgDefinBean());
                sendtime = lastmsg.getMsgDefinBean().getSendtime();
            }
        }
        baseChat.updateRoomMsg(draft, showtxt, sendtime, 0);
    }

    public boolean isOpenRecord = false;
    private PermissiomUtilNew.ResultCallBack permissomCallBack = new PermissiomUtilNew.ResultCallBack(){
                @Override
                public void granted(String[] permissions) {
                    if(permissions != null || permissions.length > 0){
                        if(permissions[0].equals(PermissiomUtilNew.PERMISSIM_RECORD_AUDIO)){
                            isOpenRecord = true;
                        }else if(permissions[0].equals(PermissiomUtilNew.PERMISSIM_STORAGE)){
                            DialogView dialogView = new DialogView();
                            dialogView.showPhotoPick(activity);
                        }
                    }
                }

                @Override
                public void deny(String[] permissions) {
                    if(permissions != null || permissions.length > 0){
                        if(permissions[0].equals(PermissiomUtilNew.PERMISSIM_RECORD_AUDIO)){
                            isOpenRecord = false;
                        }
                    }

                }
            };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        PermissiomUtilNew.getInstance().onRequestPermissionsResult(activity,requestCode,permissions,grantResults,permissomCallBack);
    }

    @Override
    protected void onStop() {
        super.onStop();
        inputPanel.hideBottomPanel();
        saveRoomInfo();
        MediaUtil.getInstance().freeMediaPlayerResource();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
