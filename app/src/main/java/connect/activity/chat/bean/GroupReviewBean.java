package connect.activity.chat.bean;

import java.io.Serializable;

import protos.Connect;

/**
 * Created by pujin on 2017/1/22.
 */

public class GroupReviewBean implements Serializable{
    private String groupKey;
    private String groupName;
    private String verificationCode;
    private String tips;
    private int source;
    private Connect.UserInfo invitor;

    public GroupReviewBean() {
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public String getGroupKey() {
        return groupKey;
    }

    public void setGroupKey(String groupKey) {
        this.groupKey = groupKey;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public Connect.UserInfo getInvitor() {
        return invitor;
    }

    public void setInvitor(Connect.UserInfo invitor) {
        this.invitor = invitor;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
