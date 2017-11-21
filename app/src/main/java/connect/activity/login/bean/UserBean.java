package connect.activity.login.bean;

import java.io.Serializable;

public class UserBean implements Serializable{

    private String name;
    private String avatar;
    private String priKey;
    private String pubKey;
    private String phone;
    private String connectId;
    private String uid;
    private boolean isOpenPassword = false;
    private boolean updateConnectId;

    public UserBean() {}

    public UserBean(String name, String avatar, String priKey, String pubKey, String phone, String connectId, String uid, boolean updateConnectId) {
        this.name = name;
        this.avatar = avatar;
        this.priKey = priKey;
        this.pubKey = pubKey;
        this.phone = phone;
        this.connectId = connectId;
        this.uid = uid;
        this.updateConnectId = updateConnectId;
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

    public boolean isOpenPassword() {
        return isOpenPassword;
    }

    public void setOpenPassword(boolean openPassword) {
        isOpenPassword = openPassword;
    }

    public boolean isUpdateConnectId() {
        return updateConnectId;
    }

    public void setUpdateConnectId(boolean updateConnectId) {
        this.updateConnectId = updateConnectId;
    }
}
