package connect.activity.chat.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * chat view type
 * Created by gtq on 2-116/11/23.
 */
public enum ItemViewType {
    NOTICE_FROM(-1, LinkMessageRow.NOTICE),
    NOTICE_TO(1, LinkMessageRow.NOTICE),
    NOTICE_ENCRYPTCHAT_TO(1, LinkMessageRow.NOTICE_ENCRYPTCHAT),

    Text_From(-1, LinkMessageRow.Text),
    Text_To(1, LinkMessageRow.Text),
    Voice_From(-1, LinkMessageRow.Voice),
    Voice_To(1, LinkMessageRow.Voice),
    Photo_From(-1, LinkMessageRow.Photo),
    Photo_To(1, LinkMessageRow.Photo),
    Video_From(-1, LinkMessageRow.Video),
    Video_To(1, LinkMessageRow.Video),
    Emotion_From(-1, LinkMessageRow.Emotion),
    Emotion_To(1, LinkMessageRow.Emotion),
    Self_destruct_Notice_From(-1, LinkMessageRow.Self_destruct_Notice),
    Self_destruct_Notice_To(1, LinkMessageRow.Self_destruct_Notice),
    Self_destruct_Receipt_From(-1, LinkMessageRow.Self_destruct_Receipt),
    Self_destruct_Receipt_To(1, LinkMessageRow.Self_destruct_Receipt),
    Request_Payment_From(-1, LinkMessageRow.Request_Payment),
    Request_Payment_To(1, LinkMessageRow.Request_Payment),
    Transfer_From_To(-1, LinkMessageRow.Transfer),
    Transfer_To(1, LinkMessageRow.Transfer),
    Lucky_Packet_From_To(-1, LinkMessageRow.Lucky_Packet),
    Lucky_Packet_To(1, LinkMessageRow.Lucky_Packet),
    Location_From(-1, LinkMessageRow.Location),
    Location_To(1, LinkMessageRow.Location),
    Name_Card_From(-1, LinkMessageRow.Name_Card),
    Name_Card_To(1, LinkMessageRow.Name_Card),
    INVITE_GROUP_From(-1, LinkMessageRow.INVITE_GROUP),
    INVITE_GROUP_To(1, LinkMessageRow.INVITE_GROUP),
    HANDLE_JOINGROUP_From(-1, LinkMessageRow.GROUP_REVIEW),
    OUTERWEBSITE_FROM(-1, LinkMessageRow.OUTER_WEBSITE),
    OUTERWEBSITE_TO(1, LinkMessageRow.OUTER_WEBSITE),
    SYSTEM_AD_FROM(-1, LinkMessageRow.SYSTEM_AD),
    SYSTEM_AD_TO(1, LinkMessageRow.SYSTEM_AD);


    public int direct;
    public LinkMessageRow messageRow;

    ItemViewType(int direct, LinkMessageRow msgType) {
        this.direct = direct;
        this.messageRow = msgType;
    }

    private static Map<Integer, ItemViewType> itemViewTypes = new HashMap();

    /**
     * key == direct*msgType
     */
    static {
        ItemViewType[] viewTypes = ItemViewType.values();
        for (ItemViewType item : viewTypes) {
            int key = item.direct * item.messageRow.type;
            itemViewTypes.put(key, item);
        }
    }

    public static ItemViewType toItemViewType(int msgType) {
        return itemViewTypes.get(msgType);
    }
}
