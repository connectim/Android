package connect.db.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class GroupMemberEntity implements Serializable {

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;

    @NotNull
    private String identifier;

    @NotNull
    private String username;

    @NotNull
    private String avatar;

    @NotNull
    private String address;
    private Integer role;
    private String nick;
    private String pub_key;
    @Generated(hash = 190136721)
    public GroupMemberEntity(Long _id, @NotNull String identifier,
            @NotNull String username, @NotNull String avatar,
            @NotNull String address, Integer role, String nick, String pub_key) {
        this._id = _id;
        this.identifier = identifier;
        this.username = username;
        this.avatar = avatar;
        this.address = address;
        this.role = role;
        this.nick = nick;
        this.pub_key = pub_key;
    }
    @Generated(hash = 1538201027)
    public GroupMemberEntity() {
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
    public String getAddress() {
        return this.address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public Integer getRole() {
        return this.role;
    }
    public void setRole(Integer role) {
        this.role = role;
    }
    public String getNick() {
        return this.nick;
    }
    public void setNick(String nick) {
        this.nick = nick;
    }
    public String getPub_key() {
        return this.pub_key;
    }
    public void setPub_key(String pub_key) {
        this.pub_key = pub_key;
    }

}
