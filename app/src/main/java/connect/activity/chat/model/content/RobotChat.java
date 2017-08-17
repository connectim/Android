package connect.activity.chat.model.content;

import com.google.protobuf.ByteString;

import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.MsgExtEntity;
import connect.database.MemoryDataManager;
import connect.im.bean.MsgType;
import connect.im.bean.SocketACK;
import connect.im.model.ChatSendManager;
import connect.ui.activity.R;
import connect.utils.TimeUtil;
import protos.Connect;

/**
 * robot message
 * Created by pujin on 2017/1/19.
 */
public class RobotChat extends NormalChat{

    private static RobotChat robotChat;

    public RobotChat() {
    }

    public static RobotChat getInstance() {
        if (robotChat == null) {
            synchronized (RobotChat.class) {
                if (robotChat == null) {
                    robotChat = new RobotChat();
                }
            }
        }
        return robotChat;
    }

    @Override
    public MsgExtEntity createBaseChat(MsgType type) {
        String mypublickey = MemoryDataManager.getInstance().getPubKey();

        MsgExtEntity msgExtEntity = new MsgExtEntity();
        msgExtEntity.setMessage_id(TimeUtil.timestampToMsgid());
        msgExtEntity.setMessage_from(mypublickey);
        msgExtEntity.setMessage_to(identify());
        msgExtEntity.setMessageType(type.type);
        msgExtEntity.setCreatetime(TimeUtil.getCurrentTimeInLong());
        msgExtEntity.setSend_status(1);
        return msgExtEntity;
    }

    @Override
    public void sendPushMsg(MsgExtEntity msgExtEntity) {
        Connect.MSMessage.Builder builder = Connect.MSMessage.newBuilder();
        builder.setCategory(msgExtEntity.getMessageType())
                .setMsgId(msgExtEntity.getMessage_id())
                .setBody(ByteString.copyFrom(msgExtEntity.getContents()));

        ChatSendManager.getInstance().sendRobotAckMsg(SocketACK.ROBOT_CHAT, identify(), builder.build());
    }

    @Override
    public String chatKey() {
        return BaseApplication.getInstance().getString(R.string.app_name);
    }

    @Override
    public int chatType() {
        return 2;
    }

    @Override
    public String identify() {
        return BaseApplication.getInstance().getString(R.string.app_name);
    }

    @Override
    public long destructReceipt() {
        return 0L;
    }

    @Override
    public Connect.MessageUserInfo senderInfo() {
        return null;
    }

    @Override
    public String headImg() {
        return BaseApplication.getInstance().getString(R.string.app_name);
    }

    @Override
    public String nickName() {
        return BaseApplication.getInstance().getString(R.string.app_name);
    }

    @Override
    public String address() {
        return BaseApplication.getInstance().getString(R.string.app_name);
    }

    public MsgExtEntity groupReviewMsg(Connect.Reviewed reviewed) {
        MsgExtEntity msgExtEntity = createBaseChat(MsgType.GROUP_REVIEW);
        msgExtEntity.setContents(reviewed.toByteArray());

        return msgExtEntity;
    }

    public MsgExtEntity outerPacketGetNoticfe(Connect.SystemRedpackgeNotice notice) {
        MsgExtEntity msgExtEntity = createBaseChat(MsgType.OUTERPACKET_GET);
        msgExtEntity.setContents(notice.toByteArray());

        return msgExtEntity;
    }

    public MsgExtEntity systemAdNotice(Connect.Announcement announcement) {
        MsgExtEntity msgExtEntity = createBaseChat(MsgType.SYSTEM_AD);
        msgExtEntity.setContents(announcement.toByteArray());

        return msgExtEntity;
    }
}
