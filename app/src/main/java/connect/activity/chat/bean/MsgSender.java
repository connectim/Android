package connect.activity.chat.bean;

import java.io.Serializable;

/**
 * Created by pujin on 2017/1/17.
 */

public class MsgSender implements Serializable {
    public String publickey;
    public String username;
    public String address;
    public String avatar;

    public MsgSender(String username, String avatar) {
        this.username = username;
        this.avatar = avatar;
    }

    public MsgSender(String pubkey, String username, String address, String avatar) {
        this.username = username;
        this.address = address;
        this.avatar = avatar;
        this.publickey = pubkey;
    }

    public String getPublickey() {
        return publickey;
    }
}
