package connect.activity.chat.bean;

import java.util.HashMap;
import java.util.Map;

import connect.activity.chat.view.row.MsgBaseRow;
import connect.activity.chat.view.row.MsgCardRow;
import connect.activity.chat.view.row.MsgEmotionRow;
import connect.activity.chat.view.row.MsgEncryptRow;
import connect.activity.chat.view.row.MsgGroupReviewRow;
import connect.activity.chat.view.row.MsgImgRow;
import connect.activity.chat.view.row.MsgInviteGroupRow;
import connect.activity.chat.view.row.MsgLocationRow;
import connect.activity.chat.view.row.MsgNoticeRow;
import connect.activity.chat.view.row.MsgPayRow;
import connect.activity.chat.view.row.MsgSysAdRow;
import connect.activity.chat.view.row.MsgTransfer;
import connect.activity.chat.view.row.MsgTxtRow;
import connect.activity.chat.view.row.MsgVideoRow;
import connect.activity.chat.view.row.MsgVoiceRow;
import connect.activity.chat.view.row.MsgWebsiteRow;
import connect.activity.chat.view.row.MsgluckRow;

/**
 * Created by Administrator on 2017/10/18.
 */

public enum LinkMessageRow {
    NOTICE(7, new MsgNoticeRow()),
    NOTICE_ENCRYPTCHAT(-500, new MsgEncryptRow()),

    Text(1, new MsgTxtRow()),
    Voice(2, new MsgVoiceRow()),
    Photo(3, new MsgImgRow()),
    Video(4, new MsgVideoRow()),
    Emotion(5, new MsgEmotionRow()),
    Request_Payment(14, new MsgPayRow()),
    Transfer(15, new MsgTransfer()),
    Lucky_Packet(16, new MsgluckRow()),
    Location(17, new MsgLocationRow()),
    Name_Card(18, new MsgCardRow()),
    INVITE_GROUP(23, new MsgInviteGroupRow()),
    OUTER_WEBSITE(25, new MsgWebsiteRow()),
    GROUP_REVIEW(101, new MsgGroupReviewRow()),
    SYSTEM_AD(102, new MsgSysAdRow());//system ad

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
