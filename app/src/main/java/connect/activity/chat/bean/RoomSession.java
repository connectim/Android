package connect.activity.chat.bean;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionHelper;
import connect.database.green.bean.ContactEntity;
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

    private static String CHAT_AVATAR="CHAT_AVATAR:";
    private Map<String, String> keyMap = new HashMap<>();
    private Connect.ChatType chatType = Connect.ChatType.PRIVATE;
    private String chatKey;
    private String chatAvatar;

    private long burntime;

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

    public long getBurntime() {
        return burntime;
    }

    public void setBurntime(long burntime) {
        this.burntime = burntime;
    }

    public String getChatAvatar() {
        String key = CHAT_AVATAR + chatKey;
        String avatar = keyMap.get(key);
        if (TextUtils.isEmpty(avatar)) {
            switch (chatType) {
                case PRIVATE:
                    Talker talker = ContactHelper.getInstance().loadTalkerFriend(chatKey);
                    avatar = talker.getAvatar();
                    keyMap.put(key, avatar);
                    break;
                case GROUPCHAT:
                    break;
                case CONNECT_SYSTEM:
                    break;
            }
        }
        return chatAvatar;
    }

    public void setChatAvatar(String chatAvatar) {
        this.chatAvatar = chatAvatar;
    }
}
