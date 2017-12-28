package connect.activity.chat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.chat.activity.BaseChatSendActivity;
import connect.activity.chat.adapter.ChatAdapter;
import connect.activity.chat.bean.LinkMessageRow;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.bean.RoomSession;
import connect.activity.chat.bean.Talker;
import connect.activity.chat.set.GroupSetActivity;
import connect.activity.chat.set.PrivateSetActivity;
import connect.activity.home.bean.ConversationAction;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.DaoHelper.ConversionSettingHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.ConversionEntity;
import connect.database.green.bean.ConversionSettingEntity;
import connect.database.green.bean.GroupEntity;
import connect.database.green.bean.GroupMemberEntity;
import connect.instant.inter.ConversationListener;
import connect.instant.model.CFriendChat;
import connect.instant.model.CGroupChat;
import connect.instant.model.CRobotChat;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import connect.utils.MediaUtil;
import connect.utils.TimeUtil;
import connect.utils.chatfile.inter.BaseFileUp;
import connect.utils.chatfile.inter.FileUploadListener;
import connect.utils.chatfile.upload.PhotoUpload;
import connect.utils.chatfile.upload.VideoUpload;
import connect.utils.log.LogManager;
import connect.utils.permission.PermissionUtil;
import connect.widget.TopToolBar;
import connect.widget.album.AlbumActivity;
import connect.widget.album.model.AlbumFile;
import connect.widget.bottominput.InputPanel;
import connect.widget.bottominput.view.InputBottomLayout;
import connect.widget.camera.CameraTakeActivity;
import connect.widget.recordvoice.RecordView;
import connect.widget.selefriend.SelectFriendActivity;
import instant.bean.ChatMsgEntity;
import instant.sender.model.NormalChat;
import protos.Connect;

/**
 * chat message
 * Created by gtq on 2016/11/22.
 */
public class ChatActivity extends BaseChatSendActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.recycler_chat)
    RecyclerView recyclerChat;
    @Bind(R.id.recordview)
    RecordView recordview;
    @Bind(R.id.inputPanel)
    InputPanel inputPanel;
    @Bind(R.id.relativelayout_1)
    RelativeLayout relativelayout1;

    private static String TAG = "_ChatActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);
        initView();
    }

    /**
     * @param activity
     * @param talker
     */
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
        toolbar.setLeftListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtil.goBack(activity);
            }
        });
        toolbar.setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String talkey = talker.getTalkKey();
                if (!TextUtils.isEmpty(talkey)) {
                    switch (talker.getTalkType()) {
                        case PRIVATE:
                            PrivateSetActivity.startActivity(activity, talkey);
                            break;
                        case GROUPCHAT:
                        case GROUP_DISCUSSION:
                            GroupSetActivity.startActivity(activity, talkey);
                            break;
                    }
                }
            }
        });
        recordview.setVisibility(View.GONE);
        inputPanel.setActivity(this);
        inputPanel.setRecordView(recordview);

        // robot/stranger donot show setting
        if (!(talker.getTalkType() == Connect.ChatType.CONNECT_SYSTEM || normalChat.isStranger())) {
            toolbar.setRightImg(R.mipmap.menu_white);
        }

        if (normalChat.chatType() == Connect.ChatType.CONNECT_SYSTEM_VALUE || normalChat.chatType() == Connect.ChatType.GROUPCHAT_VALUE) {
            roomSession.setBurntime(-1);
            updateBurnState(0);
        } else {
            ConversionSettingEntity chatSetEntity = ConversionSettingHelper.getInstance().loadSetEntity(talker.getTalkKey());
            long burntime = (chatSetEntity == null || chatSetEntity.getSnap_time() == null) ? -1 : chatSetEntity.getSnap_time();
            roomSession.setBurntime(burntime);
            updateBurnState(burntime);
        }

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(OrientationHelper.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);
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

        scrollHelper.setCheckIfItemViewFullRecycleViewForTop(true);
        scrollHelper.attachToRecycleView(recyclerChat);
        loadChatInfor();

        PermissionUtil.getInstance().requestPermission(activity,
                new String[]{PermissionUtil.PERMISSION_RECORD_AUDIO, PermissionUtil.PERMISSION_STORAGE},
                permissomCallBack);
    }

    @Override
    public void loadChatInfor() {
        LogManager.getLogger().d(TAG, "loadChatInfor()");
        new AsyncTask<Void, Void, List<ChatMsgEntity>>() {

            @Override
            protected List<ChatMsgEntity> doInBackground(Void... params) {
                return MessageHelper.getInstance().loadMoreMsgEntities(normalChat.chatKey(), TimeUtil.getCurrentTimeInLong());
            }

            @Override
            protected void onPostExecute(List<ChatMsgEntity> entities) {
                super.onPostExecute(entities);
                chatAdapter.insertItems(entities);
            }
        }.execute();
    }

    @Override
    public void loadMoreMsgs() {
        LogManager.getLogger().d(TAG, "loadMoreMsgs()");
        new AsyncTask<Void, Void, List<ChatMsgEntity>>() {
            @Override
            protected List<ChatMsgEntity> doInBackground(Void... params) {
                long lastCreateTime = 0;
                List<ChatMsgEntity> msgExtEntities = chatAdapter.getMsgEntities();
                if (msgExtEntities.size() > 0) {
                    ChatMsgEntity baseEntity = msgExtEntities.get(0);
                    lastCreateTime = baseEntity.getCreatetime();
                }
                return MessageHelper.getInstance().loadMoreMsgEntities(normalChat.chatKey(), lastCreateTime);
            }

            @Override
            protected void onPostExecute(List<ChatMsgEntity> msgEntities) {
                super.onPostExecute(msgEntities);
                if (msgEntities.size() > 0) {
                    View firstChild = recyclerChat.getChildAt(0);
                    int top = firstChild.getTop();

                    chatAdapter.insertItems(msgEntities);
                    scrollHelper.scrollToPosition(msgEntities.size(), top);//Some errors, top - SystemUtil.dipToPx(48)
                }
            }
        }.execute();
    }

    @Override
    public void updateBurnState(long time) {
        String titleName = "";
        if (time <= 0) {
            titleName = normalChat.nickName();
            if (titleName.length() > 15) {
                titleName = titleName.substring(0, 12);
                titleName += "...";
            }
            if (normalChat.chatType() == Connect.ChatType.PRIVATE_VALUE || normalChat.chatType() == Connect.ChatType.CONNECT_SYSTEM_VALUE) {
                toolbar.setTitle(titleName);
            } else {
                List<GroupMemberEntity> memEntities = ContactHelper.getInstance().loadGroupMemEntities(normalChat.chatKey());
                toolbar.setTitle(titleName + String.format(Locale.ENGLISH, "(%d)", memEntities.size()));
            }
        } else {
            String name = normalChat.nickName();
            StringBuffer indexName = new StringBuffer();
            indexName.append(name.charAt(0));
            if (name.length() > 2) {
                for (int i = 1; (i < name.length() - 1) && (i < 8); i++) {
                    indexName.append("*");
                }
            }
            indexName.append(name.charAt(name.length() - 1));
            if (normalChat.chatType() == Connect.ChatType.PRIVATE_VALUE || normalChat.chatType() == Connect.ChatType.CONNECT_SYSTEM_VALUE) {
                toolbar.setTitle(R.mipmap.message_privacy_grey2x, null);
                toolbar.setTitle(indexName.toString());
            } else {
                List<GroupMemberEntity> memEntities = ContactHelper.getInstance().loadGroupMemEntities(normalChat.chatKey());
                toolbar.setTitle(indexName + String.format(Locale.ENGLISH, "(%d)", memEntities.size()));
            }
        }
    }

    /**
     * Accept new message to slide to the end
     *
     * @param bean
     */
    @Override
    public void adapterInsetItem(ChatMsgEntity bean) {
        chatAdapter.insertItem(bean);
        if (scrollHelper.isScrollBottom()) {
            RecExtBean.getInstance().sendEventDelay(RecExtBean.ExtType.SCROLLBOTTOM);
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
                MsgSend.sendOuterMsg(MsgSend.MsgSendType.Photo, paths);
            } else if (type.equals(CameraTakeActivity.MEDIA_TYPE_VEDIO)) {
                int length = data.getIntExtra("length", 10);
                MsgSend.sendOuterMsg(MsgSend.MsgSendType.Video, path, length);
            }
        } else if (requestCode == AlbumActivity.OPEN_ALBUM_CODE && data != null) {
            List<AlbumFile> albumFiles = (List<AlbumFile>) data.getSerializableExtra("list");
            if (albumFiles != null && albumFiles.size() > 0) {
                for (AlbumFile albumFile : albumFiles) {
                    String filePath = albumFile.getPath();
                    File tempFile = new File(filePath);
                    if (tempFile.exists() && tempFile.length() > 0) {
                        if (albumFile.getMediaType() == AlbumFile.TYPE_IMAGE) {
                            List<String> paths = new ArrayList<>();
                            paths.add(albumFile.getPath());
                            MsgSend.sendOuterMsg(MsgSend.MsgSendType.Photo, paths);
                        } else {
                            int length = (int) (albumFile.getDuration() / 1000);
                            MsgSend.sendOuterMsg(MsgSend.MsgSendType.Video, albumFile.getPath(), length);
                        }
                    }
                }
            }
        } else if (requestCode == CODE_REQUEST && resultCode == RESULT_OK) {//relay the message
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

        ChatMsgEntity msgExtEntity = null;
        NormalChat normalChat = null;
        switch (roomType) {
            case 0:
                ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(roomkey);
                normalChat = new CFriendChat(friendEntity);
                break;
            case 1:
                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(roomkey);
                normalChat = new CGroupChat(groupEntity);
                break;
            case 2:
                normalChat = new CRobotChat();
                break;
        }

        BaseFileUp baseFileUp = null;
        String content = (String) objects[1];
        LinkMessageRow msgType = LinkMessageRow.toMsgType(Integer.parseInt((String) objects[0]));
        switch (msgType) {
            case Text:
                msgExtEntity = normalChat.txtMsg(content);
                MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                ((ConversationListener) normalChat).updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime());
                normalChat.sendPushMsg(msgExtEntity);
                break;
            case Photo:
                msgExtEntity = normalChat.photoMsg(content, content, FileUtil.fileSize(content),  (int)objects[2],  (int)objects[3]);
                baseFileUp = new PhotoUpload(activity, normalChat, msgExtEntity, new FileUploadListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }

                    @Override
                    public void uploadFail(int code, String message) {

                    }
                });
                baseFileUp.startUpload();
                break;
            case Video:
                String videoPath = content;
                int videoTimeLength = (int) objects[2];
                int videoSize = FileUtil.fileSizeOf(videoPath);

                Bitmap thumbBitmap = BitmapUtil.thumbVideo(videoPath);
                File thumbFile = BitmapUtil.getInstance().bitmapSavePath(thumbBitmap);
                String thumbPath = thumbFile.getAbsolutePath();

                msgExtEntity = normalChat.videoMsg(thumbPath, videoPath, videoTimeLength,
                        videoSize, thumbBitmap.getWidth(), thumbBitmap.getHeight());
                baseFileUp = new VideoUpload(activity, normalChat, msgExtEntity, new FileUploadListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }

                    @Override
                    public void uploadFail(int code, String message) {

                    }
                });
                baseFileUp.startUpload();
                break;
        }
    }

    @Override
    public void saveRoomInfo() {
        String showtxt = "";
        long sendtime = 0;
        String draft = InputBottomLayout.bottomLayout.getDraft();

        if (chatAdapter.getMsgEntities().size() != 0) {
            ChatMsgEntity lastExtEntity = chatAdapter.getMsgEntities().get(chatAdapter.getItemCount() - 1);
            if (lastExtEntity != null) {
                if (lastExtEntity.getMessageType() != -500) {
                    showtxt = lastExtEntity.showContent();
                    sendtime = lastExtEntity.getCreatetime();
                }
            }
        }

        ConversionEntity conversionEntity = ConversionHelper.getInstance().loadRoomEnitity(talker.getTalkKey());
        if (conversionEntity == null) {
            switch (talker.getTalkType()) {
                case CONNECT_SYSTEM:
                    CRobotChat.getInstance().updateRoomMsg(draft, showtxt, sendtime);
                    break;
                case PRIVATE:
                    ContactEntity contactEntity = ContactHelper.getInstance().loadFriendEntity(normalChat.chatKey());
                    if (contactEntity == null) {
                        contactEntity = new ContactEntity();
                        contactEntity.setUid(normalChat.chatKey());
                        contactEntity.setAvatar(talker.getAvatar());
                        contactEntity.setUsername(talker.getNickName());
                    }
                    CFriendChat cFriendChat = new CFriendChat(contactEntity);
                    cFriendChat.updateRoomMsg(draft, showtxt, sendtime);
                    break;
                case GROUPCHAT:
                case GROUP_DISCUSSION:
                    GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(normalChat.chatKey());
                    if (groupEntity != null) {
                        CGroupChat cGroupChat = new CGroupChat(groupEntity);
                        cGroupChat.updateRoomMsg(draft, showtxt, sendtime);
                    }
                    break;
            }
        } else {
            ConversionHelper.getInstance().updateRoomEntity(normalChat.chatKey(), draft, showtxt, sendtime);
            ConversationAction.conversationAction.sendEvent(ConversationAction.ConverType.LOAD_MESSAGE);
        }
    }

    @OnClick({R.id.relativelayout_1})
    public void onClickView(View view) {
        switch (view.getId()) {
            case R.id.relativelayout_1:
                inputPanel.hideBottomPanel();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.getInstance().onRequestPermissionsResult(activity, requestCode, permissions, grantResults, permissomCallBack);
    }

    @Override
    protected void onStop() {
        super.onStop();
        inputPanel.hideBottomPanel();
        MediaUtil.getInstance().freeMediaPlayerResource();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
