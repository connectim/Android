package connect.activity.chat.bean;

import org.greenrobot.eventbus.EventBus;
import java.io.Serializable;
import connect.activity.base.bean.BaseEvent;

/**
 * chat message, send to chat activity
 * Created by gtq on 2016/12/21.
 */
public class RecExtBean extends BaseEvent {

    public static RecExtBean recExtBean;

    public static RecExtBean getInstance() {
        if (recExtBean == null) {
            recExtBean = new RecExtBean();
        }
        return recExtBean;
    }

    @Override
    public void sendEvent(Serializable type, Serializable... objects) {
        RecExtBean recExtBean = new RecExtBean();
        recExtBean.extType = (ExtType) type;
        recExtBean.obj = objects;
        EventBus.getDefault().post(recExtBean);
    }

    public enum ExtType {
        DELMSG,//Delete the message
        RECENT_ALBUM,//Picture taken recently
        TAKE_PHOTO,//take photo
        OPEN_ALBUM,//album
        CLEAR_HISTORY,//clear message
        NOTICE,//notice
        NOTICE_NOTFRIEND,//notice not friend
        PAYMENT,//gather
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
        UPDATENAME,//update group name
        GROUP_UPDATEMYNAME,//update nick in group
        MAP_LOCATION,
        LUCKPACKET_RECEIVE,//receive a lucky packet
        GROUP_AT,//group at
        GROUP_REMOVE,//dissolution group
        UNARRIVE_UPDATE,//update friend Cookie
        UNARRIVE_HALF,//half random
        MESSAGE_RECEIVE,//receive push message
        GROUPAT_TO,//to groupat activity
        BURNREAD_SET,//Burn after reading setting
        BURNREAD_RECEIPT,//Burning receipt after reading
    }

    private ExtType extType;
    private Object obj;

    public RecExtBean() {
    }

    public RecExtBean(ExtType extType, Object obj) {
        this.extType = extType;
        this.obj = obj;
    }

    public ExtType getExtType() {
        return extType;
    }

    public Object getObj() {
        return obj;
    }
}
