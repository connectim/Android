package connect.activity.home.bean;

import java.io.Serializable;

/**
 *
 * Created by gtq on 2016/12/8.
 */
public class RoomAttrBean implements Serializable {

    private String roomid;
    private int roomtype;
    private String avatar;
    private String name;
    private long timestamp;
    private int top;
    private int disturb;
    private int unread;
    private String draft;
    private String showtxt;

    private int unreadAt;
    private int unreadAttention;

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getDisturb() {
        return disturb;
    }

    public void setDisturb(int disturb) {
        this.disturb = disturb;
    }

    public int getRoomtype() {
        return roomtype;
    }

    public void setRoomtype(int roomtype) {
        this.roomtype = roomtype;
    }

    public int getUnread() {
        return unread;
    }

    public void setUnread(int unread) {
        this.unread = unread;
    }

    public String getDraft() {
        return draft;
    }

    public void setDraft(String draft) {
        this.draft = draft;
    }

    public String getContent() {
        return showtxt;
    }

    public void setContent(String content) {
        this.showtxt = content;
    }

    public int getUnreadAt() {
        return unreadAt;
    }

    public void setUnreadAt(int unreadAt) {
        this.unreadAt = unreadAt;
    }

    public int getUnreadAttention() {
        return unreadAttention;
    }

    public void setUnreadAttention(int unreadAttention) {
        this.unreadAttention = unreadAttention;
    }
}
