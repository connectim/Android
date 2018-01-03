package connect.database.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;

@Entity
public class ContactEntity implements Serializable {

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;

    @NotNull
    @Unique
    private String uid;
    private String connectId;
    private String username;
    private String avatar;
    private String remark;
    private Integer common;
    private Integer source;
    private Boolean blocked;
    private String ou;
    private String publicKey;

    @Generated(hash = 393979869)
    public ContactEntity() {
    }

    @Generated(hash = 1040196782)
    public ContactEntity(Long _id, @NotNull String uid, String connectId,
            String username, String avatar, String remark, Integer common,
            Integer source, Boolean blocked, String ou, String publicKey) {
        this._id = _id;
        this.uid = uid;
        this.connectId = connectId;
        this.username = username;
        this.avatar = avatar;
        this.remark = remark;
        this.common = common;
        this.source = source;
        this.blocked = blocked;
        this.ou = ou;
        this.publicKey = publicKey;
    }

    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
    }
    public String getUid() {
        return this.uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getConnectId() {
        return this.connectId;
    }
    public void setConnectId(String connectId) {
        this.connectId = connectId;
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

    public String getOu() {
        return ou;
    }

    public void setOu(String ou) {
        this.ou = ou;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}
