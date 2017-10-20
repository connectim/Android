package connect.database.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class GroupEntity implements Serializable {

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;
    @NotNull
    @Unique
    private String identifier;

    private String name;
    private String ecdh_key;
    private Integer common;
    private Integer verify;
    private String avatar;
    private String summary;

    @Generated(hash = 1216626780)
    public GroupEntity(Long _id, @NotNull String identifier, String name,
            String ecdh_key, Integer common, Integer verify, String avatar,
            String summary) {
        this._id = _id;
        this.identifier = identifier;
        this.name = name;
        this.ecdh_key = ecdh_key;
        this.common = common;
        this.verify = verify;
        this.avatar = avatar;
        this.summary = summary;
    }
    @Generated(hash = 954040478)
    public GroupEntity() {
    }
    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
    }
    public String getIdentifier() {
        return this.identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEcdh_key() {
        return this.ecdh_key;
    }
    public void setEcdh_key(String ecdh_key) {
        this.ecdh_key = ecdh_key;
    }
    public Integer getCommon() {
        return this.common;
    }
    public void setCommon(Integer common) {
        this.common = common;
    }
    public Integer getVerify() {
        return this.verify;
    }
    public void setVerify(Integer verify) {
        this.verify = verify;
    }
    public String getAvatar() {
        return this.avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public String getSummary() {
        return this.summary;
    }
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
}
