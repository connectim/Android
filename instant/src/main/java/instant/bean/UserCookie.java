package instant.bean;

import java.io.Serializable;

/**
 * save user chat or socket cookie info
 * Created by pujin on 2017/4/19.
 */
public class UserCookie implements Serializable {

    private String uid;
    private String token;

    public UserCookie() {
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
}
