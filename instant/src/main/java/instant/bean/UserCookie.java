package instant.bean;

import java.io.Serializable;

/**
 * save user chat or socket cookie info
 * Created by pujin on 2017/4/19.
 */
public class UserCookie implements Serializable {

    private String uid;
    private String token;
    private String privateKey;
    private String publicKey;
    private byte[] salts;

    private String userName;
    private String userAvatar;

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

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public byte[] getSalts() {
        return salts;
    }

    public void setSalts(byte[] salts) {
        this.salts = salts;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }
}
