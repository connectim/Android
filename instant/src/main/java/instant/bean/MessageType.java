package instant.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/10/9.
 */

public enum MessageType {
    NOTICE(7),
    NOTICE_ENCRYPTCHAT(-500),

    Text(1),
    Voice(2),
    Photo(3),
    Video(4),
    Emotion(5),
    Self_destruct_Notice(11),
    Self_destruct_Receipt(12),
    Request_Payment(14),
    Transfer(15),
    Lucky_Packet(16),
    Location(17),
    Name_Card(18),
    INVITE_GROUP(23),
    OUTER_WEBSITE(25),
    GROUP_REVIEW(101),
    SYSTEM_AD(102),//system ad
    ROBOT_WAREHOSE(103);

    public int type;

    MessageType(int type) {
        this.type = type;
    }


    private static Map<Integer, MessageType> chatTypeMap = new HashMap<>();

    static {
        MessageType[] messageTypes = MessageType.values();
        for (MessageType type : messageTypes) {
            chatTypeMap.put(type.type, type);
        }
    }

    public static MessageType toMessageType(int type) {
        MessageType messageType = chatTypeMap.get(type);
        return messageType == null ? MessageType.Text : messageType;
    }
}
