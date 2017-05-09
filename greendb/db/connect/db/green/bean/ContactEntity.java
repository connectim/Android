package connect.db.green.bean;

import org.greenrobot.greendao.annotation.*;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.

/**
 * Entity mapped to table "CONTACT_ENTITY".
 */
@Entity
public class ContactEntity implements java.io.Serializable {

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

    @Generated
    public ContactEntity() {
    }

    public ContactEntity(Long _id) {
        this._id = _id;
    }

    @Generated
    public ContactEntity(Long _id, String pub_key, String address, String username, String avatar, String remark, Integer common, Integer source, Boolean blocked) {
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

    public Long get_id() {
        return _id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    @NotNull
    public String getPub_key() {
        return pub_key;
    }

    /** Not-null value; ensure this value is available before it is saved to the database. */
    public void setPub_key(@NotNull String pub_key) {
        this.pub_key = pub_key;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getCommon() {
        return common;
    }

    public void setCommon(Integer common) {
        this.common = common;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Boolean getBlocked() {
        return blocked;
    }

    public void setBlocked(Boolean blocked) {
        this.blocked = blocked;
    }

}
