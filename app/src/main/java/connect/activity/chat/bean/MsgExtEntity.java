package connect.activity.chat.bean;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import connect.activity.base.BaseApplication;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupMemberEntity;
import connect.database.green.bean.MessageEntity;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import connect.utils.StringUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import protos.Connect;

/**
 * Message extensions such as transfer information
 * Created by pujin on 2017/4/13.
 */
public class MsgExtEntity extends MessageEntity implements Cloneable {

    private String hashid;
    private int transStatus;
    private int payCount;
    private int crowdCount;
    private byte[] contents;

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

    public MessageEntity transToMessageEntity() {
        String content = getContent();
        if (TextUtils.isEmpty(content)) {
            Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(SupportKeyUril.EcdhExts.NONE, SupportKeyUril.localHashKey().getBytes(), getContents());
            content = StringUtil.bytesToHexString(gcmData.toByteArray());
        }

        MessageEntity messageEntity = new MessageEntity();
        messageEntity.set_id(get_id());
        messageEntity.setMessage_id(getMessage_id());
        messageEntity.setChatType(getChatType());
        messageEntity.setMessage_ower(getMessage_ower());
        messageEntity.setMessageType(getMessageType());
        messageEntity.setMessage_from(getMessage_from());
        messageEntity.setMessage_to(getMessage_to());
        messageEntity.setContent(content);
        messageEntity.setCreatetime(getCreatetime());
        messageEntity.setRead_time(getRead_time());
        messageEntity.setSend_status(getSend_status());
        messageEntity.setSnap_time(getSnap_time());
        return messageEntity;
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

    public long parseDestructTime() {
        long destructtime = -1;
        try {
            Connect.ChatType chatType = Connect.ChatType.forNumber(getChatType());
            if (chatType == Connect.ChatType.PRIVATE) {
                MsgType msgType = MsgType.toMsgType(getMessageType());
                switch (msgType) {
                    case Text:
                        Connect.TextMessage textMessage = Connect.TextMessage.parseFrom(getContents());
                        destructtime = textMessage.getSnapTime();
                        break;
                    case Emotion:
                        Connect.EmotionMessage emotionMessage = Connect.EmotionMessage.parseFrom(getContents());
                        destructtime = emotionMessage.getSnapTime();
                        break;
                    case Photo:
                        Connect.PhotoMessage photoMessage = Connect.PhotoMessage.parseFrom(getContents());
                        destructtime = photoMessage.getSnapTime();
                        break;
                    case Voice:
                        Connect.VoiceMessage voiceMessage = Connect.VoiceMessage.parseFrom(getContents());
                        destructtime = voiceMessage.getSnapTime();
                        break;
                    case Video:
                        Connect.VideoMessage videoMessage = Connect.VideoMessage.parseFrom(getContents());
                        destructtime = videoMessage.getSnapTime();
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return destructtime;
    }

    /**
     * Display the list in the session
     *
     * @return
     */
    public String showContent() {
        String content = "";
        MsgType msgType = MsgType.toMsgType(getMessageType());
        switch (msgType) {
            case Text://text
                if (contents != null) {
                    try {
                        Connect.TextMessage textMessage = Connect.TextMessage.parseFrom(contents);
                        content = textMessage.getContent();
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case Voice://voice
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Audio);
                break;
            case Photo://picture
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Picture);
                break;
            case Video://video
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Video);
                break;
            case Emotion://expression
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Expression);
                break;
            case Self_destruct_Notice://burn message
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Snapchat);
                break;
            case Self_destruct_Receipt://burn back
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Snapchat);
                break;
            case Request_Payment:
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Funding);
                break;
            case Transfer:
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Transfer);
                break;
            case Lucky_Packet:
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Red_packet);
                break;
            case Location:
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Location);
                break;
            case Name_Card:
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Visting_card);
                break;
            case INVITE_GROUP:
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Group_Namecard);
                break;
            case OUTER_WEBSITE:
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Group_certification);
                break;
            default:
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Tips);
                break;
        }

        Connect.ChatType chatType = Connect.ChatType.forNumber(getChatType());
        switch (chatType) {
            case PRIVATE:
                break;
            case GROUPCHAT://show group member nickname
                GroupMemberEntity memberEntity = ContactHelper.getInstance().loadGroupMemberEntity(getMessage_ower(), getMessage_from());
                if (memberEntity != null) {
                    String memberName = TextUtils.isEmpty(memberEntity.getNick()) ? memberEntity.getUsername() : memberEntity.getNick();
                    content = memberName + ": " + content;
                }
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
