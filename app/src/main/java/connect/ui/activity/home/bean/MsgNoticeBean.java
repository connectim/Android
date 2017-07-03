package connect.ui.activity.home.bean;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

/**
 *
 * Created by pujin on 2017/1/22.
 */
public class MsgNoticeBean implements Serializable{
    public enum NtEnum {
        MSG_SEND_SUCCESS,//SOCKET message send success
        MSG_SEND_FAIL,//SOCKET message send fail
    }

    public NtEnum ntEnum;
    public Object object;

    public MsgNoticeBean(NtEnum ntEnum, Object... objects) {
        this.ntEnum = ntEnum;
        this.object = objects;
    }

    public static void sendMsgNotice(NtEnum ntEnum, Object... object) {
        EventBus.getDefault().post(new MsgNoticeBean(ntEnum, object));
    }
}
