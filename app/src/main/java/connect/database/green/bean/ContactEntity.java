package connect.database.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ContactEntity implements Serializable {

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;
    @NotNull
    @Unique
    private String pub_key;
    @Unique
    private String address;

    private String username;
    private String avatar;
    private String remark;
    private Integer common;
    private Integer source;
    private Boolean blocked;

    @Generated(hash = 1202098558)
    public ContactEntity(Long _id, @NotNull String pub_key, String address,
            String username, String avatar, String remark, Integer common,
            Integer source, Boolean blocked) {
        this._id = _id;
        this.pub_key = pub_key;
        this.address = address;
        this.username = username;
        this.avatar = avatar;
        this.remark = remark;
        this.common = common;
        this.source = source;
        this.blocked = blocked;
    }
    @Generated(hash = 393979869)
    public ContactEntity() {
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
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getAvatar() {
        return this.avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public String getRemark() {
        return this.remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }
    public Integer getCommon() {
        return this.common;
    }
    public void setCommon(Integer common) {
        this.common = common;
    }
    public Integer getSource() {
        return this.source;
    }
    public void setSource(Integer source) {
        this.source = source;
    }
    public Boolean getBlocked() {
        return this.blocked;
    }
    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

}
