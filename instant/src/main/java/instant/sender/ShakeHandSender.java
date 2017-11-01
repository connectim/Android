package instant.sender;

import com.google.protobuf.ByteString;

import connect.wallet.jni.AllNativeMethod;
import instant.bean.Session;
import instant.bean.SocketACK;
import instant.bean.UserCookie;
import instant.parser.localreceiver.ConnectLocalReceiver;
import instant.utils.ConfigUtil;
import instant.utils.StringUtil;
import instant.utils.cryption.EncryptionUtil;
import instant.utils.cryption.SupportKeyUril;
import protos.Connect;

/**
 * Created by Administrator on 2017/9/30.
 */

public class ShakeHandSender {

    /**
     * A shake hands for the first time
     * @return
     */
    public void firstLoginShake() {
        ConnectLocalReceiver.receiver.requestLogin();

        UserCookie userCookie = Session.getInstance().getUserCookie(Session.CONNECT_USER);
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
        Session.getInstance().setUserCookie(Session.COOKIE_SHAKEHAND, tempCookie);

        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY,
                priKey, ConfigUtil.getInstance().serverPubKey(), newConnection.toByteString());

        String pukkey = AllNativeMethod.cdGetPubKeyFromPrivKey(priKey);
        String uid = Session.getInstance().getUserCookie(Session.CONNECT_USER).getUid();
        String signHash = SupportKeyUril.signHash(priKey, gcmData.toByteArray());
        Connect.TcpRequest imRequest = Connect.TcpRequest.newBuilder().
                setUid(uid).
                setSign(signHash).
                setCipherData(gcmData).build();

        SenderManager.getInstance().sendToMsg(SocketACK.HAND_SHAKE_FIRST, imRequest.toByteString());
    }
}
