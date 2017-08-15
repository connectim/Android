package connect.activity.chat.bean;

import connect.database.green.bean.MessageEntity;
import connect.im.bean.MsgType;
import protos.Connect;

/**
 * Message extensions such as transfer information
 * Created by pujin on 2017/4/13.
 */
public class MsgExtEntity extends MessageEntity {

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
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.set_id(get_id());
        messageEntity.setMessage_id(getMessage_id());
        messageEntity.setChatType(getChatType());
        messageEntity.setMessage_ower(getMessage_ower());
        messageEntity.setMessageType(getMessageType());
        messageEntity.setFrom(getFrom());
        messageEntity.setTo(getTo());
        messageEntity.setContent(getContent());
        messageEntity.setCreatetime(getCreatetime());
        messageEntity.setRead_time(getRead_time());
        messageEntity.setSend_status(getSend_status());
        messageEntity.setSnap_time(getSnap_time());
        messageEntity.setState(getState());
        return messageEntity;
    }

    public Connect.ChatMessage.Builder transToChatMessageBuilder() {
        Connect.ChatMessage.Builder builder = Connect.ChatMessage.newBuilder()
                .setMsgId(getMessage_id())
                .setChatType(Connect.ChatType.forNumber(getChatType()))
                .setMsgType(getMessageType())
                .setFrom(getFrom())
                .setTo(getTo())
                .setMsgTime(getCreatetime());
        return builder;
    }

    public long parseDestructTime() {
        long destructtime = 0;
        try {
            Connect.ChatType chatType = Connect.ChatType.forNumber(getChatType());
            if (chatType == Connect.ChatType.PRIVATE) {
                MsgType msgType = MsgType.toMsgType(getMessageType());
                switch (msgType) {
                    case Text:
                        Connect.TextMessage textMessage = Connect.TextMessage.parseFrom(getContents());
                        destructtime = textMessage.getSnapTime();
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

    public Connect.MessageUserInfo getUserInfo(){
        return Connect.MessageUserInfo.newBuilder().build();
    }
}
