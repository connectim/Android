package connect.activity.chat.bean;

import android.text.TextUtils;

import connect.database.SharedPreferenceUtil;
import protos.Connect;

/**
 * chat room session
 * Created by gtq on 2016/12/12.
 */
public class RoomSession {
    private static RoomSession roomSession;

    public static RoomSession getInstance() {
        if (roomSession == null) {
            synchronized (RoomSession.class) {
                if (roomSession == null) {
                    roomSession = new RoomSession();
                }
            }
        }
        return roomSession;
    }

    private int roomType = -1;
    private String roomKey;
    private String roomName;
    private String groupEcdh;
    /** 0:close burn  5000+:start burn */
    private long burntime;
    Connect.MessageUserInfo userInfo;


    public int getRoomType() {
        return roomType;
    }

    public void setRoomType(int roomtype) {
        this.roomType = roomtype;
        SharedPreferenceUtil.getInstance().putValue(SharedPreferenceUtil.ROOM_TYPE, roomtype);
    }

    public String getRoomKey() {
        if (TextUtils.isEmpty(roomKey)) {
            roomKey = SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.ROOM_KEY);
        }
        return roomKey;
    }

    public void setRoomKey(String roomKey) {
        this.roomKey = roomKey;
        SharedPreferenceUtil.getInstance().putValue(SharedPreferenceUtil.ROOM_KEY, roomKey);
    }

    public String getGroupEcdh() {
        if (TextUtils.isEmpty(groupEcdh)) {
            groupEcdh = SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.ROOM_ECDH);
        }
        return groupEcdh;
    }

    public void setGroupEcdh(String roomKey) {
        this.groupEcdh = roomKey;
        SharedPreferenceUtil.getInstance().putValue(SharedPreferenceUtil.ROOM_ECDH, roomKey);
    }

    public long getBurntime() {
        return burntime;
    }

    public void setBurntime(long burntime) {
        this.burntime = burntime;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public Connect.MessageUserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(Connect.MessageUserInfo userInfo) {
        this.userInfo = userInfo;
    }
}
