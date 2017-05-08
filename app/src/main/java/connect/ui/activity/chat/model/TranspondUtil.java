package connect.ui.activity.chat.model;

import android.app.Activity;

import connect.db.green.DaoHelper.ContactHelper;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.bean.ContactEntity;
import connect.db.green.bean.GroupEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.chat.bean.MsgChatReceiver;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.inter.FileUpLoad;
import connect.ui.activity.chat.model.content.FriendChat;
import connect.ui.activity.chat.model.content.GroupChat;
import connect.ui.activity.chat.model.content.NormalChat;
import connect.ui.activity.chat.model.fileload.PhotoUpload;
import connect.ui.activity.chat.model.fileload.VideoUpload;
import connect.utils.FileUtil;

/**
 * Created by pujin on 2017/3/17.
 */
public class TranspondUtil {

    private Activity activity;
    private int roomtype;
    private String roomkey;
    private int transtype;
    private String transcontent;

    private MsgEntity msgEntity = null;
    private FileUpLoad fileUpLoad = null;

    public TranspondUtil(Activity activity, int roomtype, String roomkey, int transtype, String transcontent) {
        this.activity = activity;
        this.roomtype = roomtype;
        this.roomkey = roomkey;
        this.transtype = transtype;
        this.transcontent = transcontent;
    }

    public void transpondTo(){
        NormalChat normalChat = null;
        switch (roomtype) {
            case 0:
                ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(roomkey);
                normalChat = new FriendChat(friendEntity);
                break;
            case 1:
                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(roomkey);
                normalChat = new GroupChat(groupEntity);
                break;
        }

        MsgType msgType = MsgType.toMsgType(transtype);
        switch (msgType) {
            case Text:
                transpondTxt(normalChat);
                break;
            case Photo:
                transpondImg(normalChat);
                break;
            case Video:
                transpondVideo(normalChat);
                break;
        }
    }

    protected void transpondTxt(NormalChat normalChat) {
        msgEntity = normalChat.txtMsg(transcontent);
        normalChat.sendPushMsg(msgEntity);
        MessageHelper.getInstance().insertToMsg(msgEntity.getMsgDefinBean());
        normalChat.updateRoomMsg("", ChatMsgUtil.showContentTxt(0, msgEntity.getMsgDefinBean()), msgEntity.getMsgDefinBean().getSendtime());

        MsgChatReceiver.sendChatReceiver(normalChat.roomKey(), msgEntity);
    }

    protected void transpondImg(final NormalChat normalChat) {
        msgEntity = normalChat.photoMsg(transcontent, FileUtil.fileSize(transcontent));
        fileUpLoad = new PhotoUpload(activity, normalChat, msgEntity.getMsgDefinBean(), new FileUpLoad.FileUpListener() {
            @Override
            public void upSuccess(Object... objs) {
                MsgEntity index = normalChat.photoMsg((String) objs[1], (String) objs[3]);

                index.getMsgDefinBean().setMessage_id((String) objs[0]);
                index.getMsgDefinBean().setUrl((String) objs[2]);
                index.getMsgDefinBean().setImageOriginWidth((Float) objs[4]);
                index.getMsgDefinBean().setImageOriginHeight((Float) objs[5]);

                normalChat.sendPushMsg(index);
                normalChat.updateRoomMsg("", ChatMsgUtil.showContentTxt(0,index.getMsgDefinBean()), index.getMsgDefinBean().getSendtime());
            }
        });
        fileUpLoad.fileHandle();

        MsgChatReceiver.sendChatReceiver(normalChat.roomKey(), msgEntity);
    }

    protected void transpondVideo(final NormalChat normalChat) {
        msgEntity = normalChat.videoMsg(transcontent, 5, FileUtil.fileSize(transcontent));
        fileUpLoad = new VideoUpload(activity, normalChat, msgEntity.getMsgDefinBean(), new FileUpLoad.FileUpListener() {
            @Override
            public void upSuccess(Object... objs) {
                MsgEntity index = normalChat.videoMsg((String) objs[1], (int) objs[3], (String) objs[4]);

                index.getMsgDefinBean().setMessage_id((String) objs[0]);
                index.getMsgDefinBean().setUrl((String) objs[2]);
                index.getMsgDefinBean().setImageOriginWidth((Float) objs[5]);
                index.getMsgDefinBean().setImageOriginHeight((Float) objs[6]);
                normalChat.sendPushMsg(index);
                normalChat.updateRoomMsg("", ChatMsgUtil.showContentTxt(0,index.getMsgDefinBean()), index.getMsgDefinBean().getSendtime());
            }
        });
        fileUpLoad.fileHandle();

        MsgChatReceiver.sendChatReceiver(normalChat.roomKey(), msgEntity);
    }
}
