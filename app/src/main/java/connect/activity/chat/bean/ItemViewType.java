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
    Location_From(-1, LinkMessageRow.Location),
    Location_To(1, LinkMessageRow.Location),
    Name_Card_From(-1, LinkMessageRow.Name_Card),
    Name_Card_To(1, LinkMessageRow.Name_Card),
    OUTERWEBSITE_FROM(-1, LinkMessageRow.OUTER_WEBSITE),
    OUTERWEBSITE_TO(1, LinkMessageRow.OUTER_WEBSITE),
    SYSTEM_AD_FROM(-1, LinkMessageRow.SYSTEM_AD),
    SYSTEM_AD_TO(1, LinkMessageRow.SYSTEM_AD),
    ROBOT_WAREHOSE_FROM(-1, LinkMessageRow.ROBOT_WAREHOSE),
    ROBOT_WAREHOSE_TO(1, LinkMessageRow.ROBOT_WAREHOSE);


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
