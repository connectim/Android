package instant.parser;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;

import connect.wallet.jni.AllNativeMethod;
import instant.bean.Session;
import instant.bean.SocketACK;
import instant.bean.UserCookie;
import instant.parser.localreceiver.ConnectLocalReceiver;
import instant.sender.SenderManager;
import instant.ui.InstantSdk;
import instant.utils.DeviceInfoUtil;
import instant.utils.SharedUtil;
import instant.utils.StringUtil;
import instant.utils.TimeUtil;
import instant.utils.XmlParser;
import instant.utils.cryption.DecryptionUtil;
import instant.utils.cryption.EncryptionUtil;
import instant.utils.cryption.SupportKeyUril;
import instant.utils.log.LogManager;
import instant.utils.manager.FailMsgsManager;
import protos.Connect;

/**
 * Created by pujin on 2017/4/18.
 */

public class ShakeHandParser extends InterParse {

    private static String TAG = "_ShakeHandParser";

    public ShakeHandParser(byte ackByte, ByteBuffer byteBuffer) {
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
        Connect.IMResponse response = Connect.IMResponse.parser().parseFrom(buffer.array());

        UserCookie userCookie = Session.getInstance().getConnectCookie();
        String uid = userCookie.getUid();
        String myPrivateKey = userCookie.getPrivateKey();

        byte[] bytes = DecryptionUtil.decodeAESGCM(EncryptionUtil.ExtendedECDH.EMPTY, myPrivateKey, XmlParser.getInstance().serverPubKey(), response.getCipherData());
        Connect.StructData structData = Connect.StructData.parseFrom(bytes);
        Connect.NewConnection newConnection = Connect.NewConnection.parser().parseFrom(structData.getPlainData());

        String token = newConnection.getToken();

        ByteString pubKey = newConnection.getPubKey();
        ByteString salt = newConnection.getSalt();
        UserCookie tempCookie = Session.getInstance().getChatCookie();
        byte[] saltXor = SupportKeyUril.xor(tempCookie.getSalts(),
                salt.toByteArray());
        byte[] ecdHkey = SupportKeyUril.getRawECDHKey(tempCookie.getPrivateKey(),
                StringUtil.bytesToHexString(pubKey.toByteArray()));
        byte[] saltByte = AllNativeMethod.cdxtalkPBKDF2HMACSHA512(ecdHkey,
                ecdHkey.length, saltXor, saltXor.length, 12, 32);
        tempCookie.setSalts(saltByte);
        tempCookie.setToken(token);
        Session.getInstance().setChatCookie(tempCookie);

        //Data encryption devices
        String deviceId = DeviceInfoUtil.getDeviceId();
        String deviceName = Build.DEVICE;
        String local = DeviceInfoUtil.getDeviceLanguage();
        String uuid = DeviceInfoUtil.getLocalUid();

        Connect.DeviceInfo deviceInfo = Connect.DeviceInfo.newBuilder()
                .setDeviceId(deviceId)
                .setDeviceName(deviceName)
                .setLocale(local)
                .setUuid(uuid)
                .setToken(token)
                .build();

        Connect.GcmData gcmDataTemp = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.NONE, saltByte, deviceInfo.toByteString());

        //imTransferData
        String signHash = SupportKeyUril.signHash(myPrivateKey, gcmDataTemp.toByteArray());
        Connect.IMTransferData imTransferData = Connect.IMTransferData.newBuilder()
                .setCipherData(gcmDataTemp)
                .setSign(signHash)
                .setToken(token)
                .setUid(uid)
                .build();

        SenderManager.getInstance().sendToMsg(SocketACK.HAND_SHAKE_SECOND, imTransferData.toByteString());
    }

    /**
     * connect success
     */
    private void connectSuccess() {
        LogManager.getLogger().d(TAG, "===>  connectSuccess");
        try {
            ConnectLocalReceiver.receiver.loginSuccess();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String version = SharedUtil.getInstance().getStringValue(SharedUtil.CONTACTS_VERSION);
        if (TextUtils.isEmpty(version)) {
            int welcomeVersion = SharedUtil.getInstance().getIntValue(SharedUtil.WELCOME_VERSION);//version == 0 ,the first time login
            if (welcomeVersion == 0) {
                SharedUtil.getInstance().putValue(SharedUtil.WELCOME_VERSION, ++welcomeVersion);
                welcomeMessage();
            }
        }

        requestFriendsByVersion();
        connectLogin();
        pullOffLineMsg();
        checkCurVersion();
        FailMsgsManager.getInstance().sendFailMsgs();
    }

    private void welcomeMessage() {
        ConnectLocalReceiver.receiver.welcome();
    }

    /**
     * login
     */
    private void connectLogin() {
        String deviceId = DeviceInfoUtil.getDeviceId();
        Connect.DeviceToken deviceToken = Connect.DeviceToken.newBuilder()
                .setDeviceId(deviceId)
                .setPushType("GCM").build();

        String msgid = TimeUtil.timestampToMsgid();
        commandToIMTransfer(msgid, SocketACK.CONTACT_LOGIN, deviceToken.toByteString());
    }

    /**
     * pull offline message
     */
    protected void pullOffLineMsg() {
        ConnectLocalReceiver.receiver.pullOfflineMessage();

        String msgid = TimeUtil.timestampToMsgid();
        SenderManager.getInstance().sendAckMsg(SocketACK.PULL_OFFLINE, null, msgid, ByteString.copyFrom(new byte[]{}));
    }

    /**
     * current version
     */
    protected void checkCurVersion() {
        Context context = InstantSdk.getInstance().getBaseContext();
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);

            String oldVersion = SharedUtil.getInstance().getStringValue(SharedUtil.UPLOAD_APPINFO_VERSION);
            if (TextUtils.isEmpty(oldVersion)) {
                SharedUtil.getInstance().putValue(SharedUtil.UPLOAD_APPINFO_VERSION, "1");

                String versionName = packageInfo.versionName;
                Connect.AppInfo appInfo = Connect.AppInfo.newBuilder()
                        .setVersion(versionName)
                        .setModel(Build.MODEL)
                        .setOsVersion(Build.VERSION.RELEASE)
                        .setPlatform("android").build();

                String msgid = TimeUtil.timestampToMsgid();
                commandToIMTransfer(msgid, SocketACK.UPLOAD_APPINFO, appInfo.toByteString());
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}
