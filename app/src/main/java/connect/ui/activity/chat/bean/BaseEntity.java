package connect.ui.activity.chat.bean;

import com.google.gson.Gson;

import java.io.Serializable;

import connect.db.green.bean.MessageEntity;

/**
 * Created by pujin on 2017/1/19.
 */

public class BaseEntity implements Serializable {
    private int sendstate;
    private int readstate;
    private String pubkey;

    private String msgid;
    private String hashid;
    private int transStatus;
    private int payCount;
    private int crowdCount;

    private MsgDefinBean msgDefinBean;

    public int getSendstate() {
        return sendstate;
    }

    public void setSendstate(int sendstate) {
        this.sendstate = sendstate;
    }

    public int getReadstate() {
        return readstate;
    }

    public void setReadstate(int readstate) {
        this.readstate = readstate;
    }

    public MsgDefinBean getMsgDefinBean() {
        return msgDefinBean;
    }

    public void setMsgDefinBean(MsgDefinBean msgDefinBean) {
        this.msgDefinBean = msgDefinBean;
    }

    public String getPubkey() {
        return pubkey;
    }

    public void setPubkey(String pubkey) {
        this.pubkey = pubkey;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
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

    public MessageEntity transToChatEntity() {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setMessage_id(getMsgDefinBean().getMessage_id());
        messageEntity.setContent(new Gson().toJson(getMsgDefinBean()));
        messageEntity.setState(getReadstate());
        messageEntity.setMessage_ower(getPubkey());
        messageEntity.setState(getSendstate());
        return messageEntity;
    }
}
