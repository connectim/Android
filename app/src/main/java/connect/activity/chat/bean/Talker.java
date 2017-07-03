package connect.activity.chat.bean;

import android.text.TextUtils;

import java.io.Serializable;

import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupEntity;

/**
 * chat talker
 * Created by pujin on 2017/2/10.
 */
public class Talker implements Serializable{
    private int talkType;
    private String talkKey;
    private String talkAvatar;
    private String talkName;
    private String talkAddress;

    private ContactEntity friendEntity;
    private GroupEntity groupEntity;

    public Talker(ContactEntity entity) {
        this.friendEntity = entity;
        this.talkType = 0;
        this.talkKey = friendEntity.getPub_key();
        this.talkAvatar = friendEntity.getAvatar();
        String username = TextUtils.isEmpty(friendEntity.getUsername()) ? friendEntity.getRemark() : friendEntity.getUsername();
        this.talkName = username;
        this.talkAddress = friendEntity.getAddress();
    }

    public Talker(GroupEntity entity) {
        this.groupEntity = entity;
        this.talkType = 1;
        this.talkKey = groupEntity.getIdentifier();
        this.talkAvatar = groupEntity.getAvatar();
        this.talkName = groupEntity.getName();
        this.talkAddress = "";
    }

    public Talker(int talkType, String talkKey) {
        this.talkType = talkType;
        this.talkKey = talkKey;
    }

    public int getTalkType() {
        return talkType;
    }

    public String getTalkKey() {
        return talkKey;
    }

    public String getTalkAvatar() {
        return talkAvatar;
    }

    public String getTalkName() {
        return talkName;
    }

    public String getTalkAddress() {
        return talkAddress;
    }

    public ContactEntity getFriendEntity() {
        return friendEntity;
    }

    public void setFriendEntity(ContactEntity friendEntity) {
        this.friendEntity = friendEntity;
    }

    public GroupEntity getGroupEntity() {
        return groupEntity;
    }

    public void setGroupEntity(GroupEntity groupEntity) {
        this.groupEntity = groupEntity;
    }
}
