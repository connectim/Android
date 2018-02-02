package connect.activity.home.bean;

import org.greenrobot.eventbus.EventBus;

import java.io.Serializable;

import connect.activity.base.bean.BaseEvent;

/**
 * Created by pujin on 2017/2/10.
 */
public class HomeAction extends BaseEvent {

    public static HomeAction homeAction;

    public static HomeAction getInstance() {
        if (homeAction == null) {
            homeAction = new HomeAction();
        }
        return homeAction;
    }

    public enum HomeType {
        DELAY_EXIT,// 5S, Quit to the main interface
        EXIT,// Quit to the main interface
        SWITCHFRAGMENT,// switch to fragment

        TOCHAT_FRIEND,
        TOCHAT_GROUP,
        TOCHAT_ROBOT,

        GROUP_NEWCHAT,
        REMOTE_LOGIN;
    }

    @Override
    public void sendEvent(Serializable type, Serializable... objects) {
        HomeAction homeAction = new HomeAction();
        homeAction.type = (HomeType) type;
        homeAction.object = objects;
        EventBus.getDefault().post(homeAction);
    }

    public HomeType type;
    public Object object;

    public HomeAction() {
    }

    public HomeType getType() {
        return type;
    }

    public Object getObject() {
        return object;
    }
}
