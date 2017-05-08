package connect.ui.activity.chat.bean;

import java.util.HashMap;
import java.util.Map;

import connect.im.bean.MsgType;

/**
 * chat view type
 * Created by gtq on 2-116/11/23.
 */
public enum ItemViewType {
    GROUP_INVITE(1, MsgType.GROUP_INVITE),
    NOTICE_FROM(-1, MsgType.NOTICE),
    NOTICE_TO(1, MsgType.NOTICE),
    NOTICE_STRANGER_FROM(-1, MsgType.NOTICE_STRANGER),
    NOTICE_STRANGER_TO(1, MsgType.NOTICE_STRANGER),
    NOTICE_BLACK_FROM(-1, MsgType.NOTICE_BLACK),
    NOTICE_BLACK_TO(1, MsgType.NOTICE_BLACK),
    NOTICE_NOTMEMBER_FROM(-1, MsgType.NOTICE_NOTMEMBER),
    NOTICE_NOTMEMBER_TO(1, MsgType.NOTICE_NOTMEMBER),
    NOTICE_ENCRYPTCHAT_TO(1, MsgType.NOTICE_ENCRYPTCHAT),
    NOTICE_GROUPADDUSER_TO(1, MsgType.GROUP_ADDUSER_TO),
    NOTICE_CLICKRECEIVEPACKET_TO(1, MsgType.NOTICE_CLICKRECEIVEPACKET),

    Text_From(-1, MsgType.Text),
    Text_To(1, MsgType.Text),
    Voice_From(-1, MsgType.Voice),
    Voice_To(1, MsgType.Voice),
    Photo_From(-1, MsgType.Photo),
    Photo_To(1, MsgType.Photo),
    Video_From(-1, MsgType.Video),
    Video_To(1, MsgType.Video),
    Emotion_From(-1, MsgType.Emotion),
    Emotion_To(1, MsgType.Emotion),
    Self_destruct_Notice_From(-1, MsgType.Self_destruct_Notice),
    Self_destruct_Notice_To(1, MsgType.Self_destruct_Notice),
    Self_destruct_Receipt_From(-1, MsgType.Self_destruct_Receipt),
    Self_destruct_Receipt_To(1, MsgType.Self_destruct_Receipt),
    Request_Payment_From(-1, MsgType.Request_Payment),
    Request_Payment_To(1, MsgType.Request_Payment),
    Transfer_From_To(-1, MsgType.Transfer),
    Transfer_To(1, MsgType.Transfer),
    Lucky_Packet_From_To(-1, MsgType.Lucky_Packet),
    Lucky_Packet_To(1, MsgType.Lucky_Packet),
    Location_From(-1, MsgType.Location),
    Location_To(1, MsgType.Location),
    Name_Card_From(-1, MsgType.Name_Card),
    Name_Card_To(1, MsgType.Name_Card),
    INVITE_GROUP_From(-1, MsgType.INVITE_GROUP),
    INVITE_GROUP_To(1, MsgType.INVITE_GROUP),
    HANDLE_JOINGROUP_From(-1, MsgType.GROUP_REVIEW),
    OUTERWEBSITE_FROM(-1, MsgType.OUTER_WEBSITE),
    OUTERWEBSITE_TO(1, MsgType.OUTER_WEBSITE),
    REDPACKET_GET_FROM(-1, MsgType.OUTERPACKET_GET),
    REDPACKET_GET_TO(1, MsgType.OUTERPACKET_GET),
    SYSTEM_AD_FROM(-1, MsgType.SYSTEM_AD),
    SYSTEM_AD_TO(1, MsgType.SYSTEM_AD);

    public int direct;
    public MsgType msgType;

    ItemViewType(int direct, MsgType msgType) {
        this.direct = direct;
        this.msgType = msgType;
    }

    private static Map<Integer, ItemViewType> itemViewTypes = new HashMap();

    /**
     * key == direct*msgType
     */
    static {
        ItemViewType[] viewTypes = ItemViewType.values();
        for (ItemViewType item : viewTypes) {
            int key = item.direct * item.msgType.type;
            itemViewTypes.put(key, item);
        }
    }

    public static ItemViewType toItemViewType(int msgType) {
        return itemViewTypes.get(msgType);
    }
}
