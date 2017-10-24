package connect.activity.login.bean;

import java.io.Serializable;

public class UserBean implements Serializable{

    private String talkKey;
    private String name;
    private String avatar;
    private String passHint;
    private String priKey;
    private String pubKey;
    private String phone;
    private String connectId;
    private String uid;
    private String patterStr;

    public UserBean() {
    }

    public UserBean(String talkKey, String name, String avatar, String passHint, String priKey, String pubKey, String phone,String connectId) {
        this.talkKey = talkKey;
        this.name = name;
        this.avatar = avatar;
        this.passHint = passHint;
        this.priKey = priKey;
        this.pubKey = pubKey;
        this.phone = phone;
        this.connectId = connectId;
    }

    public String getTalkKey() {
        return talkKey;
    }

    public void setTalkKey(String talkKey) {
        this.talkKey = talkKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPassHint() {
        return passHint;
    }

    public void setPassHint(String passHint) {
        this.passHint = passHint;
    }

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getConnectId() {
        return connectId;
    }

    public void setConnectId(String connectId) {
        this.connectId = connectId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPatterStr() {
        return patterStr;
    }

    public void setPatterStr(String patterStr) {
        this.patterStr = patterStr;
    }
}
