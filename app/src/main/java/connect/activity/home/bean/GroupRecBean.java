package connect.activity.home.bean;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Administrator on 2017/11/8.
 */

public class GroupRecBean {

    public enum GroupRecType {
        GroupInfo,//group information
        GroupNotificaton,//Mute Notification
    }

    public GroupRecType groupRecType;
    public Object obj;


    public GroupRecBean(GroupRecType groupRecType, Object obj) {
        this.groupRecType = groupRecType;
        this.obj = obj;
    }

    public static void sendGroupRecMsg(GroupRecType recType, Object... objs) {
        EventBus.getDefault().post(new GroupRecBean(recType, objs));
    }
}
