package connect.database.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

@Entity
public class GroupMemberEntity implements Serializable {

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;

    @NotNull
    private String identifier;
    @NotNull
    private String uid;
    @NotNull
    private String username;
    @NotNull
    private String avatar;
    private Integer role;

    @Generated(hash = 1538201027)
    public GroupMemberEntity() {
    }

    @Generated(hash = 896721694)
    public GroupMemberEntity(Long _id, @NotNull String identifier,
            @NotNull String uid, @NotNull String username, @NotNull String avatar,
            Integer role) {
        this._id = _id;
        this.identifier = identifier;
        this.uid = uid;
        this.username = username;
        this.avatar = avatar;
        this.role = role;
    }

    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
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
    public Integer getRole() {
        return this.role;
    }
    public void setRole(Integer role) {
        this.role = role;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
}
