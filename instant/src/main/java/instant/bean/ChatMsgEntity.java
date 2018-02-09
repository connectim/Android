package instant.bean;

import android.content.Context;
import android.text.TextUtils;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.Serializable;

import instant.R;
import instant.ui.InstantSdk;
import instant.utils.StringUtil;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/9.
 */
public final class ChatMsgEntity implements Serializable, Cloneable {

    static final long serialVersionUID = 42L;

    private Long _id;
    private String message_ower;
    private String message_id;

    private int chatType;
    private String message_from;
    private String message_to;
    private int messageType;

    private String content;
    private Long read_time;
    private Integer send_status;
    private Long snap_time;
    private Long createtime;

    private String hashid;
    private int transStatus;
    private int payCount;
    private int crowdCount;
    private byte[] contents;
    // 发送方
    private String from_avatar;
    private String from_username;

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public String getMessage_ower() {
        return message_ower;
    }

    public void setMessage_ower(String message_ower) {
        this.message_ower = message_ower;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public int getChatType() {
        return chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public String getMessage_from() {
        return message_from;
    }

    public void setMessage_from(String message_from) {
        this.message_from = message_from;
    }

    public String getMessage_to() {
        return message_to;
    }

    public void setMessage_to(String message_to) {
        this.message_to = message_to;
    }

    public int getMessageType() {
        return messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getRead_time() {
        return read_time;
    }

    public void setRead_time(Long read_time) {
        this.read_time = read_time;
    }

    public Integer getSend_status() {
        return send_status;
    }

    public void setSend_status(Integer send_status) {
        this.send_status = send_status;
    }

    public Long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Long createtime) {
        this.createtime = createtime;
    }

    public String getHashid() {
        return hashid;
    }

    public void setHashid(String hashid) {
        this.hashid = hashid;
    }

    public int getTransStatus() {
        return transStatus;
    }

    public void setTransStatus(int transStatus) {
        this.transStatus = transStatus;
    }

    public int getPayCount() {
        return payCount;
    }

    public void setPayCount(int payCount) {
        this.payCount = payCount;
    }

    public int getCrowdCount() {
        return crowdCount;
    }

    public void setCrowdCount(int crowdCount) {
        this.crowdCount = crowdCount;
    }

    public byte[] getContents() {
        return contents;
    }

    public void setContents(byte[] contents) {
        this.contents = contents;
    }

    public Long getSnap_time() {
        return snap_time;
    }

    public void setSnap_time(Long snap_time) {
        this.snap_time = snap_time;
    }

    public String getFrom_avatar() {
        return from_avatar;
    }

    public void setFrom_avatar(String from_avatar) {
        this.from_avatar = from_avatar;
    }

    public String getFrom_username() {
        return from_username;
    }

    public void setFrom_username(String from_username) {
        this.from_username = from_username;
    }

    public Connect.ChatMessage.Builder transToChatMessageBuilder() {
        Connect.ChatMessage.Builder builder = Connect.ChatMessage.newBuilder()
                .setMsgId(getMessage_id())
                .setChatType(Connect.ChatType.forNumber(getChatType()))
                .setMsgType(getMessageType())
                .setFrom(getMessage_from())
                .setTo(getMessage_to())
                .setMsgTime(getCreatetime());
        return builder;
    }

    public static ChatMsgEntity transToMessageEntity(String messageid, String messageowner, int chattype, int messagetype, String from, String to, byte[] contents, long createtime, int sendstate) {
        ChatMsgEntity messageEntity = new ChatMsgEntity();
        messageEntity.setMessage_id(messageid);
        messageEntity.setMessage_ower(messageowner);
        messageEntity.setChatType(chattype);
        messageEntity.setMessageType(messagetype);
        messageEntity.setMessage_from(from);
        messageEntity.setMessage_to(to);
        messageEntity.setContent(StringUtil.bytesToHexString(contents));
        messageEntity.setContents(contents);
        messageEntity.setCreatetime(createtime);
        messageEntity.setSend_status(sendstate);
        messageEntity.setRead_time(0L);

        return messageEntity;
    }

    public MsgDirect parseDirect() {
        UserCookie userCookie = Session.getInstance().getConnectCookie();
        String mypubkey = userCookie == null ? "" : userCookie.getUid();
        return mypubkey.equals(getMessage_from()) ? MsgDirect.To : MsgDirect.From;
    }

    /**
     * Display the list in the session
     *
     * @return
     */
    public String showContent() {
        String content = "";

        Context context = InstantSdk.getInstance().getBaseContext();
        MessageType msgType = MessageType.toMessageType(getMessageType());
        switch (msgType) {
            case Text://text
                if (contents != null) {
                    try {
                        Connect.TextMessage textMessage = Connect.TextMessage.parseFrom(contents);
                        content = textMessage.getContent();
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }else if(!TextUtils.isEmpty(this.content)){
                    try {
                        Connect.TextMessage textMessage = Connect.TextMessage.parseFrom(StringUtil.hexStringToBytes(this.content));
                        content = textMessage.getContent();
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Voice://voice
                content = context.getString(R.string.Chat_Audio);
                break;
            case Photo://picture
                content = context.getString(R.string.Chat_Picture);
                break;
            case Video://video
                content = context.getString(R.string.Chat_Video);
                break;
            case Emotion://expression
                content = context.getString(R.string.Chat_Expression);
                break;
            case Self_destruct_Notice://burn message
                content = context.getString(R.string.Chat_Snapchat);
                break;
            case Self_destruct_Receipt://burn back
                content = context.getString(R.string.Chat_Snapchat);
                break;
            case Request_Payment:
                content = context.getString(R.string.Chat_Funding);
                break;
            case Transfer:
                content = context.getString(R.string.Chat_Transfer);
                break;
            case Lucky_Packet:
                content = context.getString(R.string.Chat_Red_packet);
                break;
            case Location:
                content = context.getString(R.string.Chat_Location);
                break;
            case Name_Card:
                content = context.getString(R.string.Chat_Visting_card);
                break;
            case INVITE_GROUP:
                content = context.getString(R.string.Chat_Group_Namecard);
                break;
            case OUTER_WEBSITE:
                content = context.getString(R.string.Chat_Warehouse);
                break;
            default:
                content = context.getString(R.string.Chat_Tips);
                break;
        }

        Connect.ChatType chatType = Connect.ChatType.forNumber(getChatType());
        switch (chatType) {
            case PRIVATE:
                break;
            case GROUPCHAT://show group member nickname
//                GroupMemberEntity memberEntity = ContactHelper.getInstance().loadGroupMemberEntity(getMessage_ower(), getMessage_from());
//                if (memberEntity != null) {
//                    String memberName = TextUtils.isEmpty(memberEntity.getNick()) ? memberEntity.getUsername() : memberEntity.getNick();
//                    content = memberName + ": " + content;
//                }
                break;
            case CONNECT_SYSTEM:
                break;
        }
        return content;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
