package instant.sender;

import com.google.protobuf.ByteString;

import connect.wallet.jni.AllNativeMethod;
import instant.bean.Session;
import instant.bean.SocketACK;
import instant.bean.UserCookie;
import instant.parser.localreceiver.ConnectLocalReceiver;
import instant.utils.XmlParser;
import instant.utils.StringUtil;
import instant.utils.cryption.EncryptionUtil;
import instant.utils.cryption.SupportKeyUril;
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
        String priKey = userCookie.getPriKey();

        String randomPriKey = AllNativeMethod.cdCreateNewPrivKey();
        String randomPubKey = AllNativeMethod.cdGetPubKeyFromPrivKey(randomPriKey);

        String cdSeed = AllNativeMethod.cdCreateSeed(16, 4);
        Connect.NewConnection newConnection = Connect.NewConnection.newBuilder().
                setPubKey(ByteString.copyFrom(StringUtil.hexStringToBytes(randomPubKey))).
                setSalt(ByteString.copyFrom(cdSeed.getBytes())).build();

        UserCookie tempCookie = new UserCookie();
        tempCookie.setPriKey(randomPriKey);
        tempCookie.setPubKey(randomPubKey);
        tempCookie.setSalt(cdSeed.getBytes());
        Session.getInstance().setRandomCookie(tempCookie);

        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY,
                priKey, XmlParser.getInstance().serverPubKey(), newConnection.toByteString());

        String uid = Session.getInstance().getConnectCookie().getUid();
        String signHash = SupportKeyUril.signHash(priKey, gcmData.toByteArray());
        Connect.TcpRequest imRequest = Connect.TcpRequest.newBuilder()
                .setUid(uid)
                .setSign(signHash)
                .setCipherData(gcmData)
                .build();

        SenderManager.getInstance().sendToMsg(SocketACK.HAND_SHAKE_FIRST, imRequest.toByteString());
    }
}
