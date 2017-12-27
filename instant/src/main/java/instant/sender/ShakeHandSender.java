package instant.sender;

import instant.bean.Session;
import instant.bean.SocketACK;
import instant.bean.UserCookie;
import instant.parser.localreceiver.ConnectLocalReceiver;
import instant.utils.DeviceInfoUtil;
import instant.utils.log.LogManager;
import protos.Connect;

/**
 * Created by Administrator on 2017/9/30.
 */

public class ShakeHandSender {

    private static String TAG = "ShakeHandSender";

    /**
     * A shake hands for the first time
     * @return
     */
    public void firstLoginShake() {
        try {
            ConnectLocalReceiver.receiver.requestLogin();
        } catch (Exception e) {
            e.printStackTrace();
            LogManager.getLogger().d(TAG, e.getMessage());
        }

        UserCookie userCookie = Session.getInstance().getConnectCookie();
        String uid = userCookie.getUid();
        String token = userCookie.getToken();

        Connect.NewConnection newConnection = Connect.NewConnection.newBuilder()
                .setToken(token)
                .build();
        Connect.StructData structData = Connect.StructData.newBuilder()
                .setPlainData(newConnection.toByteString())
                .build();

        String deviceId = DeviceInfoUtil.getDeviceId();
        Connect.TcpRequest imRequest = Connect.TcpRequest.newBuilder()
                .setUid(uid)
                .setToken(token)
                .setDeviceId(deviceId)
                .setBody(structData.toByteString())
                .build();

        SenderManager.getInstance().sendToMsg(SocketACK.HAND_SHAKE_FIRST, imRequest.toByteString());
    }
}
