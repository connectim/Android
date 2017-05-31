package connect.ui.activity.chat.bean;

import org.greenrobot.eventbus.EventBus;

/**
 * chat message, send to chat activity
 * Created by gtq on 2016/12/21.
 */
public class RecExtBean {

    public enum ExtType {
        DELMSG,//Delete the message
        RECENT_ALBUM,//Picture taken recently
        TAKE_PHOTO,//take photo
        OPEN_ALBUM,//album
        CLEAR_HISTORY,//clear message
        NOTICE,//notice
        NOTICE_NOTFRIEND,//notice not friend
        GATHER,//gather
        TRANSFER,//transaction
        REDPACKET,//lucky packet
        NAMECARD,//name card
        MSGSTATE,//message send state 0:seding 1:send success 2:send fail 3:send refuse
        MSGSTATEVIEW,//Update message status
        RESEND,//resend message
        IMGVIEWER,//preview image message
        SCROLLBOTTOM,
        HIDEPANEL,
        VOICE_UNREAD,//Voice did not read, read the next unread voice message
        VOICE_COMPLETE,//Audio message natural finish
        VOICE_RELEASE,//Audio message forced to stop
        BURNSTATE,//burn message update state
        BURNMSG_READ,//start burn message
        GROUP_UPDATENAME,//update group name
        GROUP_UPDATEMYNAME,//update nick in group
        MAP_LOCATION,
        LUCKPACKET_RECEIVE,//receive a lucky packet
        GROUP_AT,//group at
        GROUP_REMOVE,//dissolution group
        UNARRIVE_UPDATE,//update friend Cookie
        UNARRIVE_HALF,//half random
        MESSAGE_RECEIVE,//receive push message
    }

    private ExtType extType;
    private Object obj;

    public RecExtBean(ExtType extType, Object obj) {
        this.extType = extType;
        this.obj = obj;
    }

    public static void sendRecExtMsg(ExtType type, Object... objs) {
        EventBus.getDefault().post(new RecExtBean(type, objs));
    }

    public ExtType getExtType() {
        return extType;
    }

    public Object getObj() {
        return obj;
    }
}
