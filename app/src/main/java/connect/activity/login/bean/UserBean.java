package connect.activity.login.bean;

import java.io.Serializable;

public class UserBean implements Serializable{

    private String name;
    private String avatar;
    private String uid;
    private String token;
    private String o_u;

    public UserBean() {}

    public UserBean(String name, String avatar, String uid, String o_u, String token) {
        this.name = name;
        this.avatar = avatar;
        this.uid = uid;
        this.o_u = o_u;
        this.token = token;
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getO_u() {
        return o_u;
    }

    public void setO_u(String o_u) {
        this.o_u = o_u;
    }
}
