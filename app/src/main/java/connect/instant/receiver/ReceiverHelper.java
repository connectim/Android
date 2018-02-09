package connect.instant.receiver;

import android.content.Context;

import connect.activity.base.BaseApplication;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import instant.parser.localreceiver.CommandLocalReceiver;
import instant.parser.localreceiver.ConnectLocalReceiver;
import instant.parser.localreceiver.ExceptionLocalReceiver;
import instant.parser.localreceiver.MessageLocalReceiver;
import instant.parser.localreceiver.RobotLocalReceiver;
import instant.parser.localreceiver.TransactionLocalReceiver;
import instant.parser.localreceiver.UnreachableLocalReceiver;
import instant.ui.InstantSdk;
import instant.utils.log.LogManager;

/**
 * Created by Administrator on 2017/10/25.
 */

public class ReceiverHelper {

    private static String TAG = "_ReceiverHelper";

    public void initInstantSDK() {
        Context context = BaseApplication.getInstance().getBaseContext();

        UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
        long count = ContactHelper.getInstance().contactsCount();

        InstantSdk.getInstance().registerUserInfo(context,
                userBean.getUid(),
                userBean.getPriKey(),
                userBean.getPubKey(),
                userBean.getToken(),
                userBean.getName(),
                userBean.getAvatar(),
                count);

        try {
            ConnectLocalReceiver.receiver.registerConnect(ConnectReceiver.receiver);
            CommandLocalReceiver.receiver.registerCommand(CommandReceiver.receiver);
            TransactionLocalReceiver.localReceiver.registerTransactionListener(TransactionReceiver.receiver);
            RobotLocalReceiver.localReceiver.registerRobotListener(RobotReceiver.receiver);
            UnreachableLocalReceiver.localReceiver.registerUnreachableListener(UnreachableReceiver.receiver);
            MessageLocalReceiver.localReceiver.registerMessageListener(MessageReceiver.receiver);
            ExceptionLocalReceiver.localReceiver.registerConnect(ExceptionReceiver.receiver);
        } catch (Exception e) {
            e.printStackTrace();
            LogManager.getLogger().d(TAG, e.getMessage());
        }
    }
}
