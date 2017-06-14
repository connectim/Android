package connect.ui.activity.chat.bean;

import java.io.Serializable;

/**
 * card  Extended information
 * Created by gtq on 2016/12/13.
 */
public class CardExt1Bean implements Serializable{
    private String username;
    private String avatar;
    private String pub_key;
    private String address;

    public CardExt1Bean() {
    }

    public CardExt1Bean(String username, String avatar, String pub_key, String address) {
        this.username = username;
        this.avatar = avatar;
        this.pub_key = pub_key;
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

    public String getPub_key() {
        return pub_key;
    }

    public void setPub_key(String pub_key) {
        this.pub_key = pub_key;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
