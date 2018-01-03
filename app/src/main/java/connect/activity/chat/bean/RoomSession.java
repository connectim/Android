package connect.activity.chat.bean;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.ConversionSettingHelper;
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
    private String chatPublicKey;

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

    public void checkBurnTime(long time) {
        if (burntime != time) {
            this.burntime = time;
            ConversionSettingHelper.getInstance().updateBurnTime(chatKey, time);
            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.BURNREAD_SET, chatKey, time);
        }
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
        return avatar;
    }

    public void updateChatAvatar(String friendUid,String newAvatar){
        String key = CHAT_AVATAR + friendUid;
        String avatar = keyMap.get(key);
        if (!TextUtils.isEmpty(avatar)) {
            keyMap.put(key, newAvatar);
        }
    }

    public String getChatPublicKey() {
        return chatPublicKey;
    }

    public void setChatPublicKey(String chatPublicKey) {
        this.chatPublicKey = chatPublicKey;
    }
}
