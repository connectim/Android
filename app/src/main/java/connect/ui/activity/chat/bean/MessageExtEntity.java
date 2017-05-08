package connect.ui.activity.chat.bean;

import connect.db.green.bean.MessageEntity;

/**
 * Created by pujin on 2017/4/13.
 */
public class MessageExtEntity extends MessageEntity{
    private String hashid;
    private int transStatus;
    private int payCount;
    private int crowdCount;

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
}
