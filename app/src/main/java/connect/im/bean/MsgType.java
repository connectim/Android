package connect.im.bean;

import java.util.HashMap;
import java.util.Map;

import connect.activity.chat.view.row.MsgSysAdRow;
import connect.activity.chat.view.row.MsgBaseRow;
import connect.activity.chat.view.row.MsgCardRow;
import connect.activity.chat.view.row.MsgDestructRow;
import connect.activity.chat.view.row.MsgEmotionRow;
import connect.activity.chat.view.row.MsgGroupReviewRow;
import connect.activity.chat.view.row.MsgImgRow;
import connect.activity.chat.view.row.MsgInviteGroupRow;
import connect.activity.chat.view.row.MsgLocationRow;
import connect.activity.chat.view.row.MsgNoticeRow;
import connect.activity.chat.view.row.MsgPayRow;
import connect.activity.chat.view.row.MsgTransfer;
import connect.activity.chat.view.row.MsgTxtRow;
import connect.activity.chat.view.row.MsgVideoRow;
import connect.activity.chat.view.row.MsgVoiceRow;
import connect.activity.chat.view.row.MsgWebsiteRow;
import connect.activity.chat.view.row.MsgluckRow;

/**
 * chat mesage type
 * Created by gtq on 2016/11/23.
 */
public enum MsgType {
    GROUP_INVITE(-6, new MsgNoticeRow()),
    NOTICE(7, new MsgNoticeRow()),
    NOTICE_ENCRYPTCHAT(-500, new MsgDestructRow()),
    NOTICE_CLICKRECEIVEPACKET(-501, new MsgNoticeRow()),
    GETPACKET_FROM(-8, new MsgNoticeRow()),
    GETPACEKT_TO(8, new MsgNoticeRow()),
    GROUP_ADDUSER_TO(-22, new MsgNoticeRow()),
    NOTICE_STRANGER(-9, new MsgNoticeRow()),
    NOTICE_BLACK(-10, new MsgNoticeRow()),
    NOTICE_NOTMEMBER(-11, new MsgNoticeRow()),

    Text(1, new MsgTxtRow()),
    Voice(2, new MsgVoiceRow()),
    Photo(3, new MsgImgRow()),
    Video(4, new MsgVideoRow()),
    Emotion(5, new MsgEmotionRow()),
    Self_destruct_Notice(11, new MsgNoticeRow()),
    Self_destruct_Receipt(12, new MsgNoticeRow()),
    Request_Payment(14, new MsgPayRow()),
    Transfer(15, new MsgTransfer()),
    Lucky_Packet(16, new MsgluckRow()),
    Location(17, new MsgLocationRow()),
    Name_Card(18, new MsgCardRow()),
    INVITE_GROUP(23, new MsgInviteGroupRow()),
    OUTER_WEBSITE(25, new MsgWebsiteRow()),
    GROUP_REVIEW(101, new MsgGroupReviewRow()),
    SYSTEM_AD(102, new MsgSysAdRow()),//system ad
    OUTERPACKET_GET(103, new MsgNoticeRow());//outer red packet was to receive

    public int type;
    public MsgBaseRow msgBaseRow;

    MsgType(int type, MsgBaseRow msgBaseRow) {
        this.type = type;
        this.msgBaseRow = msgBaseRow;
    }

    private static Map<Integer, MsgType> msgTypeMap = new HashMap<>();

    static {
        MsgType[] msgTypes = MsgType.values();
        for (MsgType type : msgTypes) {
            msgTypeMap.put(type.type, type);
        }
    }

    public static MsgType toMsgType(int type) {
        return msgTypeMap.get(type);
    }
}
