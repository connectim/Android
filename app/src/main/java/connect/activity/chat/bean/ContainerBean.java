package connect.activity.chat.bean;

import org.greenrobot.eventbus.EventBus;

/**
 * Dynamic update container components display
 * Created by pujin on 2017/3/1.
 */
public class ContainerBean {
    public enum ContainerType {
        ROBOT_HANDLEAPPLY,//Robot news
        GATHER_DETAIL,//gather detail
        TRANSFER_STATE,//transaction state
    }

    private ContainerBean.ContainerType extType;
    private Object obj;

    public ContainerBean(ContainerBean.ContainerType extType, Object obj) {
        this.extType = extType;
        this.obj = obj;
    }

    public static void sendRecExtMsg(ContainerBean.ContainerType type, Object... objs) {
        EventBus.getDefault().post(new ContainerBean(type, objs));
    }

    public ContainerBean.ContainerType getExtType() {
        return extType;
    }

    public Object getObj() {
        return obj;
    }
}
