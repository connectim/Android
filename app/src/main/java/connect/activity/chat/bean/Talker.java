package connect.activity.chat.bean;

import java.io.Serializable;

import protos.Connect;

/**
 * chat talker
 * Created by pujin on 2017/2/10.
 */
public class Talker implements Serializable {

    private Connect.ChatType talkType = Connect.ChatType.PRIVATE;
    /** Uid */
    private String talkKey = "";

    private String nickName;
    private String avatar;
    private boolean isStranger;
    private String friendPublicKey;

    public Talker(Connect.ChatType talkType, String talkKey) {
        this.talkType = talkType;
        this.talkKey = talkKey;
    }

    public Connect.ChatType getTalkType() {
        return talkType;
    }

    public String getTalkKey() {
        return talkKey;
    }


    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isStranger() {
        return isStranger;
    }

    public void setStranger(boolean stranger) {
        isStranger = stranger;
    }

    public String getFriendPublicKey() {
        return friendPublicKey;
    }

    public void setFriendPublicKey(String friendPublicKey) {
        this.friendPublicKey = friendPublicKey;
    }
}
