package connect.database.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class RecommandFriendEntity implements Serializable {

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;

    @NotNull
    @Unique
    private String pub_key;

    @NotNull
    private String username;

    @NotNull
    @Unique
    private String address;

    @NotNull
    private String avatar;
    private Integer status;
    @Generated(hash = 1185893177)
    public RecommandFriendEntity(Long _id, @NotNull String pub_key,
            @NotNull String username, @NotNull String address,
            @NotNull String avatar, Integer status) {
        this._id = _id;
        this.pub_key = pub_key;
        this.username = username;
        this.address = address;
        this.avatar = avatar;
        this.status = status;
    }
    @Generated(hash = 217496388)
    public RecommandFriendEntity() {
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
    public String getUsername() {
        return this.username;
    }
    public void setUsername(String username) {
        this.username = username;
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
    public Integer getStatus() {
        return this.status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }

}
