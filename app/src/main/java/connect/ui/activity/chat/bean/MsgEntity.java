package connect.ui.activity.chat.bean;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by gtq on 2016/12/2.
 */
public class MsgEntity implements Serializable {

    private String recAddress;
    private long burnstarttime;

    private int sendstate;
    private int readstate;
    private String pubkey;

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
        return msgDefinBean.getMessage_id();
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

    public String getRecAddress() {
        return recAddress;
    }

    public void setRecAddress(String recAddress) {
        this.recAddress = recAddress;
    }

    public long getBurnstarttime() {
        return burnstarttime;
    }

    public void setBurnstarttime(long burnstarttime) {
        this.burnstarttime = burnstarttime;
    }

    public RoomType getRoomType() {
        RoomType roomType = null;
        String pubkey = msgDefinBean.getPublicKey();
        if (TextUtils.isEmpty(pubkey) || pubkey.length() < 10) {
            roomType = RoomType.RobotType;
        } else if (pubkey.length() == 40) {
            roomType = RoomType.GroupType;
        } else {
            roomType = RoomType.FriendType;
        }
        return roomType;
    }
}