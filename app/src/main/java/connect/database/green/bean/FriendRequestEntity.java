package connect.database.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class FriendRequestEntity implements Serializable {

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;

    @NotNull
    private String pub_key;

    @NotNull
    private String address;
    private String avatar;
    private String username;
    private String tips;
    private Integer source;
    private Integer status;
    private Integer read;
    @Generated(hash = 1099393682)
    public FriendRequestEntity(Long _id, @NotNull String pub_key,
            @NotNull String address, String avatar, String username, String tips,
            Integer source, Integer status, Integer read) {
        this._id = _id;
        this.pub_key = pub_key;
        this.address = address;
        this.avatar = avatar;
        this.username = username;
        this.tips = tips;
        this.source = source;
        this.status = status;
        this.read = read;
    }
    @Generated(hash = 651915895)
    public FriendRequestEntity() {
    }
    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
    }
    public String getPub_key() {
        return this.pub_key;
    }
    public void setPub_key(String pub_key) {
        this.pub_key = pub_key;
    }
    public String getAddress() {
        return this.address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getAvatar() {
        return this.avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getTips() {
        return this.tips;
    }
    public void setTips(String tips) {
        this.tips = tips;
    }
    public Integer getSource() {
        return this.source;
    }
    public void setSource(Integer source) {
        this.source = source;
    }
    public Integer getStatus() {
        return this.status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
    public Integer getRead() {
        return this.read;
    }
    public void setRead(Integer read) {
        this.read = read;
    }

}
