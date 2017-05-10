package connect.im.bean;

import android.os.Build;
import android.text.TextUtils;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;

import connect.db.SharePreferenceUser;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.MessageHelper;
import connect.db.green.DaoHelper.ParamManager;
import connect.im.inter.InterParse;
import connect.im.model.ConnectManager;
import connect.im.model.FailMsgsManager;
import connect.im.model.MsgSendManager;
import connect.im.msgdeal.SendMsgUtil;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgSender;
import connect.ui.activity.chat.bean.RoMsgEntity;
import connect.ui.activity.chat.model.ChatMsgUtil;
import connect.ui.activity.chat.model.content.RobotChat;
import connect.ui.activity.home.bean.MsgFragmReceiver;
import connect.ui.base.BaseApplication;
import connect.utils.ConfigUtil;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.system.SystemDataUtil;
import connect.wallet.jni.AllNativeMethod;
import protos.Connect;

/**
 * Created by pujin on 2017/4/18.
 */

public class ShakeHandBean extends InterParse {

    public ShakeHandBean(byte ackByte, ByteBuffer byteBuffer) {
        super(ackByte, byteBuffer);
    }

    @Override
    public void msgParse() throws Exception {
        switch (ackByte) {
            case 0x01://The first handshake messages
                shakeMsgSend(byteBuffer);
                break;
            case 0x02://connect success
                connectSuccess();
                break;
        }
    }

    private void shakeMsgSend(ByteBuffer buffer) throws Exception {
        ConnectState.getInstance().sendEvent(ConnectState.ConnectType.REFRESH_SUCCESS);

        Connect.IMResponse response = null;
        response = Connect.IMResponse.parser().parseFrom(buffer.array());

        if (!SupportKeyUril.verifySign(response.getSign(), response.getCipherData().toByteArray())) {
            throw new Exception("verifySign ");
        }

        String priKey = SharedPreferenceUtil.getInstance().getPriKey();
        byte[] bytes = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.EMPTY, priKey, ConfigUtil.getInstance().serverPubkey(), response.getCipherData());
        Connect.StructData structData = Connect.StructData.parseFrom(bytes);
        Connect.NewConnection newConnection = Connect.NewConnection.parser().parseFrom(structData.getPlainData());

        ByteString pubKey = newConnection.getPubKey();
        ByteString salt = newConnection.getSalt();
        UserCookie tempCookie = Session.getInstance().getUserCookie("TEMPCOOKIE");
        byte[] saltXor = SupportKeyUril.xor(tempCookie.getSalt(),
                salt.toByteArray(), salt.size());
        byte[] ecdHkey = SupportKeyUril.rawECDHkey(tempCookie.getPriKey(),
                StringUtil.bytesToHexString(pubKey.toByteArray()));
        byte[] saltByte = AllNativeMethod.cdxtalkPBKDF2HMACSHA512(ecdHkey,
                ecdHkey.length, saltXor, saltXor.length, 12, 32);
        tempCookie.setSalt(saltByte);
        Session.getInstance().setUserCookie("TEMPCOOKIE", tempCookie);

        //Data encryption devices
        String deviceId = SystemDataUtil.getDeviceId();
        String deviceName = Build.DEVICE;
        String local = SystemDataUtil.getDeviceLanguage();
        String uuid=SystemDataUtil.getLocalUid();
        Connect.DeviceInfo deviceInfo = Connect.DeviceInfo.newBuilder()
                .setDeviceId(deviceId)
                .setDeviceName(deviceName)
                .setLocale(local)
                .setCv(0)
                .setUuid(uuid).build();
        Connect.GcmData gcmDataTemp = EncryptionUtil.encodeAESGCMStructData(SupportKeyUril.EcdhExts.NONE, saltByte, deviceInfo.toByteString());

        //imTransferData
        String signHash = SupportKeyUril.signHash(priKey, gcmDataTemp.toByteArray());
        Connect.IMTransferData imTransferData = Connect.IMTransferData.newBuilder().
                setCipherData(gcmDataTemp).
                setSign(signHash).build();

        MsgSendManager.getInstance().sendMessage(SocketACK.HAND_SHAKE_SECOND.getOrder(), imTransferData.toByteArray());
    }

    /**
     * connect success
     */
    private void connectSuccess() {
        ConnectState.getInstance().sendEventDelay(ConnectState.ConnectType.CONNECT);

        ConnectManager.getInstance().connectSuccess();
        String version = ParamManager.getInstance().getString(ParamManager.COUNT_FRIENDLIST);
        if (TextUtils.isEmpty(version)) {
            int vrsion = SharePreferenceUser.getInstance().getIntValue(SharePreferenceUser.CONTACT_VERSION);
            if (vrsion == 0) {//the first time login
                SharePreferenceUser.getInstance().putInt(SharePreferenceUser.CONTACT_VERSION, 1);
                welcomeRobotMsg();
            }
        }

        SendMsgUtil.requestFriendsByVersion();
        SendMsgUtil.connectLogin();
        SendMsgUtil.pullOffLineMsg();
        FailMsgsManager.getInstance().sendFailMsgs();
        SendMsgUtil.checkCurVersion();
    }

    private void welcomeRobotMsg() {
        RoMsgEntity entity = RobotChat.getInstance().txtMsg(BaseApplication.getInstance().getString(R.string.Login_Welcome));
        entity.getMsgDefinBean().setSenderInfoExt(new MsgSender(RobotChat.getInstance().nickName(), ""));
        MessageHelper.getInstance().insertFromMsg(RobotChat.getInstance().roomKey(), entity.getMsgDefinBean());

        ChatMsgUtil.updateRoomInfo(RobotChat.getInstance().roomKey(), 2, TimeUtil.getCurrentTimeInLong(), entity.getMsgDefinBean());
        MsgFragmReceiver.refreshRoom(MsgFragmReceiver.FragRecType.ALL);
    }
}
