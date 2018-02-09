package connect.activity.home.bean;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

/**
 * Created by gtq on 2016/12/23.
 */

public class HttpRecBean implements Serializable {

    public enum HttpRecType {
        SALTEXPIRE,//salt timeout
        SALT_VERIFY,//salt verify
        PrivateSet,//private setting
        BlackList,//black list
        UpLoadBackUp,//upload backup
        DownBackUp,//get backup by myself
        DownGroupBackUp,//get backup by group
        SOUNDPOOL,//system voice
        SYSTEM_VIBRATION,//system vibrate
        GroupNotificaton,//Mute Notification
    }

    public HttpRecType httpRecType;
    public Object obj;


    public HttpRecBean(HttpRecType httpRecType, Object obj1) {
        this.httpRecType = httpRecType;
        this.obj = obj1;
    }

    public static void sendHttpRecMsg(HttpRecType recType, Object... objs) {
        EventBus.getDefault().post(new HttpRecBean(recType, objs));
    }
}
