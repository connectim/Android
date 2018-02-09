package connect.activity.chat.activity;

import android.os.Bundle;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import connect.activity.base.BaseListener;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.bean.RoomSession;
import connect.activity.chat.exts.GoogleMapActivity;
import connect.activity.chat.exts.GroupAtActivity;
import connect.activity.chat.set.ContactCardActivity;
import connect.activity.home.HomeActivity;
import connect.database.green.DaoHelper.MessageHelper;
import connect.utils.ActivityUtil;
import connect.utils.chatfile.inter.BaseFileUp;
import connect.utils.chatfile.inter.FileUploadListener;
import connect.utils.chatfile.upload.LocationUpload;
import connect.utils.chatfile.upload.PhotoUpload;
import connect.utils.chatfile.upload.VideoUpload;
import connect.utils.chatfile.upload.VoiceUpload;
import connect.utils.permission.PermissionUtil;
import connect.widget.album.AlbumActivity;
import connect.widget.bottominput.InputPanel;
import connect.widget.camera.CameraTakeActivity;
import connect.widget.imagewatcher.ImageViewerActivity;
import instant.bean.ChatMsgEntity;
import instant.bean.MessageType;
import instant.sender.model.GroupChat;

/**
 * Created by Administrator on 2017/10/31.
 */

public abstract class BaseChatReceiveActivity extends BaseChatActvity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                InputPanel.inputPanel.hideBottomPanel();
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
                if (chatIdentify.equals(objects[0])) {
                    chatAdapter.clearHistory();
                    MessageHelper.getInstance().deleteRoomMsg(chatIdentify);
                }
                break;
            case NAMECARD:
                ContactCardActivity.startActivity(activity,chatIdentify);
                break;
            case MSGSTATE://message send state 0:sending 1:send success 2:send fail 3:send refuse
                if (chatIdentify.equals(objects[0])) {
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
            case UPDATENAME://update group name
                String identify = (String) objects[0];
                if (identify.equals(normalChat.chatKey())) {
                    updateTitleName();
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
                    // ((FriendChat) normalChat).setFriendCookie(null);
                }
                break;
            case UNARRIVE_HALF:
                break;
            case GROUPAT_TO:
                GroupAtActivity.startActivity(activity, normalChat.chatKey());
                break;
            case MESSAGE_RECEIVE:
                if (objects[0].equals(chatIdentify)) {
                    msgExtEntity = (ChatMsgEntity) objects[1];
                    adapterInsetItem(msgExtEntity);
                }
                break;
        }
    }


    protected void reSendFailMsg(ChatMsgEntity msgExtEntity) {
        BaseFileUp upLoad = null;
        MessageType msgType = MessageType.toMessageType(msgExtEntity.getMessageType());
        switch (msgType) {
            case Photo://picture message
                upLoad = new PhotoUpload(activity, normalChat, msgExtEntity, new FileUploadListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }

                    @Override
                    public void uploadFail(int code, String message) {

                    }
                });
                upLoad.startUpload();
                break;
            case Voice://voice message
                upLoad = new VoiceUpload(activity, normalChat, msgExtEntity, new FileUploadListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }

                    @Override
                    public void uploadFail(int code, String message) {

                    }
                });
                upLoad.startUpload();
                break;
            case Video://video message
                upLoad = new VideoUpload(activity, normalChat, msgExtEntity, new FileUploadListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }

                    @Override
                    public void uploadFail(int code, String message) {

                    }
                });
                upLoad.startUpload();
                break;
            case Location://location message
                upLoad = new LocationUpload(activity, normalChat, msgExtEntity, new FileUploadListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }

                    @Override
                    public void uploadFail(int code, String message) {

                    }
                });
                upLoad.startUpload();
                break;
            default:
                normalChat.sendPushMsg(msgExtEntity);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
