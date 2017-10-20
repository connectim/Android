package instant.bean;

import java.io.Serializable;

/**
 * save user chat or socket cookie info
 * Created by pujin on 2017/4/19.
 */
public class UserCookie implements Serializable {

    private String priKey;
    private String pubKey;
    private byte[] salt;
    private long expiredTime;

    public UserCookie() {
    }

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public long getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(long expiredTime) {
        this.expiredTime = expiredTime;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }
}
