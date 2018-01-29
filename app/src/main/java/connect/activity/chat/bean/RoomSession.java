package connect.activity.chat.bean;

import protos.Connect;

/**
 * chat room session
 * Created by gtq on 2016/12/12.
 */
public class RoomSession {

    public static RoomSession roomSession = getInstance();

    public synchronized static RoomSession getInstance() {
        if (roomSession == null) {
            roomSession = new RoomSession();
        }
        return roomSession;
    }

    private Connect.ChatType chatType = Connect.ChatType.PRIVATE;
    private String chatKey;
    private String friendAvatar;

    public Connect.ChatType getRoomType() {
        return chatType;
    }

    public void setRoomType(Connect.ChatType roomtype) {
        this.chatType = roomtype;
    }

    public String getRoomKey() {
        return chatKey;
    }

    public void setRoomKey(String roomKey) {
        this.chatKey = roomKey;
    }

    public String getFriendAvatar() {
        return friendAvatar;
    }

    public void setFriendAvatar(String friendAvatar) {
        this.friendAvatar = friendAvatar;
    }
}
