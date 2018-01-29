package connect.instant.receiver;

import connect.activity.home.bean.HomeAction;
import instant.parser.inter.ExceptionListener;

/**
 * Created by Administrator on 2017/11/8.
 */

public class ExceptionReceiver implements ExceptionListener{

    private static String TAG = "_ExceptionReceiver";

    public static ExceptionReceiver receiver = getInstance();

    private synchronized static ExceptionReceiver getInstance() {
        if (receiver == null) {
            receiver = new ExceptionReceiver();
        }
        return receiver;
    }

    @Override
    public void remoteLogin(String devicename) {
        HomeAction.getInstance().sendEvent(HomeAction.HomeType.REMOTE_LOGIN,devicename);
    }

    @Override
    public void exitAccount() {
        HomeAction.getInstance().sendEvent(HomeAction.HomeType.DELAY_EXIT);
    }
}
