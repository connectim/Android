package instant.sender;

import com.google.protobuf.ByteString;

import connect.wallet.jni.AllNativeMethod;
import instant.bean.Session;
import instant.bean.SocketACK;
import instant.bean.UserCookie;
import instant.parser.localreceiver.ConnectLocalReceiver;
import instant.utils.StringUtil;
import instant.utils.XmlParser;
import instant.utils.cryption.EncryptionUtil;
import instant.utils.cryption.SupportKeyUril;
import instant.utils.log.LogManager;
import protos.Connect;

/**
 * Created by Administrator on 2017/9/30.
 */

public class ShakeHandSender {

    private static String TAG = "_ShakeHandSender";

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
        String priKey = userCookie.getPrivateKey();

        LogManager.getLogger().d(TAG, "uid: " + uid);
        LogManager.getLogger().d(TAG, "token: " + token);
        LogManager.getLogger().d(TAG, "priKey: " + priKey);

        String randomPriKey = AllNativeMethod.cdCreateNewPrivKey();
        String randomPubKey = AllNativeMethod.cdGetPubKeyFromPrivKey(randomPriKey);

        String cdSeed = AllNativeMethod.cdCreateSeed(16, 4);
        UserCookie tempCookie = new UserCookie();
        tempCookie.setPrivateKey(randomPriKey);
        tempCookie.setPublicKey(randomPubKey);
        tempCookie.setSalts(cdSeed.getBytes());
        tempCookie.setToken(token);
        Session.getInstance().setChatCookie(tempCookie);

        Connect.NewConnection newConnection = Connect.NewConnection.newBuilder()
                .setPubKey(ByteString.copyFrom(StringUtil.hexStringToBytes(randomPubKey)))
                .setSalt(ByteString.copyFrom(cdSeed.getBytes()))
                .setToken(token)
                .build();

        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY,
                priKey, XmlParser.getInstance().serverPubKey(), newConnection.toByteString());

        String signHash = SupportKeyUril.signHash(priKey, gcmData.toByteArray());
        Connect.TcpRequest tcpRequest = Connect.TcpRequest.newBuilder()
                .setUid(uid)
                .setCipherData(gcmData)
                .setSign(signHash)
                .build();

        SenderManager.getInstance().sendToMsg(SocketACK.HAND_SHAKE_FIRST, tcpRequest.toByteString());
    }
}
