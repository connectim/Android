package connect.activity.chat.bean;

import connect.database.green.bean.MessageEntity;

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
}
