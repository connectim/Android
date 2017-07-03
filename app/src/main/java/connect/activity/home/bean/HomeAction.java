package connect.activity.home.bean;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by pujin on 2017/2/10.
 */
public class HomeAction {
    public enum HomeType {
        DELAY_EXIT,//5S , Quit to the main interface
        EXIT,//Quit to the main interface
        TOCHAT,//to chat interface
    }

    private HomeType type;
    private Object object;

    public HomeAction(HomeType type, Object object) {
        this.type = type;
        this.object = object;
    }

    public static void sendTypeMsg(HomeType type, Object... obj) {
        EventBus.getDefault().post(new HomeAction(type, obj));
    }

    public HomeType getType() {
        return type;
    }

    public Object getObject() {
        return object;
    }
}
