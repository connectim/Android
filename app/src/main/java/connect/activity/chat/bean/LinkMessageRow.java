package connect.activity.chat.bean;

import java.util.HashMap;
import java.util.Map;

import connect.activity.chat.view.row.MsgBaseRow;
import connect.activity.chat.view.row.MsgCardRow;
import connect.activity.chat.view.row.MsgEmotionRow;
import connect.activity.chat.view.row.MsgImgRow;
import connect.activity.chat.view.row.MsgLocationRow;
import connect.activity.chat.view.row.MsgNoticeRow;
import connect.activity.chat.view.row.MsgSysAdRow;
import connect.activity.chat.view.row.MsgTxtRow;
import connect.activity.chat.view.row.MsgVideoRow;
import connect.activity.chat.view.row.MsgVoiceRow;
import connect.activity.chat.view.row.MsgWarehouseRow;
import connect.activity.chat.view.row.MsgWebsiteRow;

/**
 * Created by Administrator on 2017/10/18.
 */

public enum LinkMessageRow {
    NOTICE(7, new MsgNoticeRow()),

    Text(1, new MsgTxtRow()),
    Voice(2, new MsgVoiceRow()),
    Photo(3, new MsgImgRow()),
    Video(4, new MsgVideoRow()),
    Location(17, new MsgLocationRow()),
    Name_Card(18, new MsgCardRow()),
    Emotion(5, new MsgEmotionRow()),
    OUTER_WEBSITE(25, new MsgWebsiteRow()),
    SYSTEM_AD(102, new MsgSysAdRow()),//system ad
    ROBOT_WAREHOSE(103, new MsgWarehouseRow());

    public int type;
    public MsgBaseRow msgBaseRow;

    LinkMessageRow(int type, MsgBaseRow msgBaseRow) {
        this.type = type;
        this.msgBaseRow = msgBaseRow;
    }

    private static Map<Integer, LinkMessageRow> msgTypeMap = new HashMap<>();

    static {
        LinkMessageRow[] msgTypes = LinkMessageRow.values();
        for (LinkMessageRow type : msgTypes) {
            msgTypeMap.put(type.type, type);
        }
    }

    public static LinkMessageRow toMsgType(int type) {
        LinkMessageRow msgType = msgTypeMap.get(type);
        return msgType == null ? LinkMessageRow.Text : msgType;
    }
}
