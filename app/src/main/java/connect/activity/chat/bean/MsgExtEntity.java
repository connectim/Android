package connect.activity.chat.bean;

import connect.database.SharedPreferenceUtil;
import connect.database.green.bean.MessageEntity;
import instant.bean.MsgDirect;
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


    public MsgDirect parseDirect() {
        String mypubkey = SharedPreferenceUtil.getInstance().getUser().getUid();
        return mypubkey.equals(getMessage_from()) ? MsgDirect.To : MsgDirect.From;
    }

    public MessageEntity transToMessageEntity() {
        String content = getContent();

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
}
