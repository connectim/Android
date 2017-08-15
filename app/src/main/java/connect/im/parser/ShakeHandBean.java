package connect.im.parser;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;

import java.nio.ByteBuffer;

import connect.activity.chat.bean.MsgExtEntity;
import connect.database.MemoryDataManager;
import connect.database.SharePreferenceUser;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.ParamHelper;
import connect.database.green.DaoHelper.ParamManager;
import connect.database.green.bean.ParamEntity;
import connect.im.bean.ConnectState;
import connect.im.bean.Session;
import connect.im.bean.SocketACK;
import connect.im.bean.UserCookie;
import connect.im.inter.InterParse;
import connect.im.model.ChatSendManager;
import connect.im.model.FailMsgsManager;
import connect.ui.activity.R;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.MsgSender;
import connect.activity.chat.model.content.RobotChat;
import connect.activity.base.BaseApplication;
import connect.service.bean.PushMessage;
import connect.service.bean.ServiceAck;
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

        String priKey = MemoryDataManager.getInstance().getPriKey();
        byte[] bytes = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.EMPTY, priKey, ConfigUtil.getInstance().serverPubkey(), response.getCipherData());
        Connect.StructData structData = Connect.StructData.parseFrom(bytes);
        Connect.NewConnection newConnection = Connect.NewConnection.parser().parseFrom(structData.getPlainData());

        ByteString pubKey = newConnection.getPubKey();
        ByteString salt = newConnection.getSalt();
        UserCookie tempCookie = Session.getInstance().getUserCookie("TEMPCOOKIE");
        byte[] saltXor = SupportKeyUril.xor(tempCookie.getSalt(),
                salt.toByteArray());
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

        Connect.DeviceInfo.Builder deviceBuilder = Connect.DeviceInfo.newBuilder()
                .setDeviceId(deviceId)
                .setDeviceName(deviceName)
                .setLocale(local)
                .setCv(0)
                .setUuid(uuid);

        String pubkey = MemoryDataManager.getInstance().getPubKey();
        String cookieKey = "COOKIE:" + pubkey;
        ParamEntity paramEntity = ParamHelper.getInstance().likeParamEntityDESC(cookieKey);//local cookie
        if (paramEntity != null) {
            UserCookie userCookie = new Gson().fromJson(paramEntity.getValue(), UserCookie.class);
            Connect.ChatCookieData cookieData = Connect.ChatCookieData.newBuilder().
                    setChatPubKey(userCookie.getPubKey()).
                    setSalt(ByteString.copyFrom(userCookie.getSalt())).
                    setExpired(userCookie.getExpiredTime()).build();
            deviceBuilder.setChatCookieData(cookieData);
        }

        Connect.DeviceInfo deviceInfo = deviceBuilder.build();
        Connect.GcmData gcmDataTemp = EncryptionUtil.encodeAESGCMStructData(SupportKeyUril.EcdhExts.NONE, saltByte, deviceInfo.toByteString());

        //imTransferData
        String signHash = SupportKeyUril.signHash(priKey, gcmDataTemp.toByteArray());
        Connect.IMTransferData imTransferData = Connect.IMTransferData.newBuilder().
                setCipherData(gcmDataTemp).
                setSign(signHash).build();

        ChatSendManager.getInstance().sendToMsg(SocketACK.HAND_SHAKE_SECOND, imTransferData.toByteString());
    }

    /**
     * connect success
     */
    private void connectSuccess() {
        ConnectState.getInstance().sendEventDelay(ConnectState.ConnectType.CONNECT);

        PushMessage.pushMessage(ServiceAck.CONNECT_SUCCESS,new byte[0],ByteBuffer.allocate(0));
        String version = ParamManager.getInstance().getString(ParamManager.COUNT_FRIENDLIST);
        if (TextUtils.isEmpty(version)) {
            requestFriendsByVersion();

            int vrsion = SharePreferenceUser.getInstance().getIntValue(SharePreferenceUser.CONTACT_VERSION);
            if (vrsion == 0) {//the first time login
                SharePreferenceUser.getInstance().putInt(SharePreferenceUser.CONTACT_VERSION, 1);
                welcomeRobotMsg();
            }
        }

        connectLogin();
        pullOffLineMsg();
        checkCurVersion();
        FailMsgsManager.getInstance().sendFailMsgs();
    }

    private void welcomeRobotMsg() {
        String mypublickey = MemoryDataManager.getInstance().getPubKey();
        MsgExtEntity msgExtEntity = RobotChat.getInstance().txtMsg(BaseApplication.getInstance().getString(R.string.Login_Welcome));
        msgExtEntity.setFrom(RobotChat.getInstance().nickName());
        msgExtEntity.setTo(mypublickey);

        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
        RobotChat.getInstance().updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime(), -1, true);
    }

    /**
     * login
     */
    private void connectLogin() {
        String deviceId = SystemDataUtil.getDeviceId();
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
        String msgid = TimeUtil.timestampToMsgid();
        ChatSendManager.getInstance().sendMsgidMsg(SocketACK.PULL_OFFLINE, msgid, ByteString.copyFrom(new byte[]{}));
    }

    /**
     * current version
     */
    protected void checkCurVersion() {
        Context context = BaseApplication.getInstance().getBaseContext();
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            String versionCode = String.valueOf(packageInfo.versionCode);
            String versionName = packageInfo.versionName;

            boolean newVersion = false;
            ParamEntity paramEntity = ParamHelper.getInstance().loadParamEntity("device_version");
            if (paramEntity == null) {
                paramEntity = new ParamEntity();
                paramEntity.setKey("device_version");
                newVersion = true;
            } else {
                newVersion = !versionName.equals(paramEntity.getValue());
            }

            if (newVersion) {
                paramEntity.setValue(versionName);
                ParamHelper.getInstance().insertParamEntity(paramEntity);

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

    /**
     * A shake hands for the first time
     *
     * @return
     */
    public void firstLoginShake() {
        ConnectState.getInstance().sendEvent(ConnectState.ConnectType.REFRESH_ING);

        String priKey = MemoryDataManager.getInstance().getPriKey();
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
        Session.getInstance().setUserCookie("TEMPCOOKIE", tempCookie);

        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(SupportKeyUril.EcdhExts.EMPTY,
                priKey, ConfigUtil.getInstance().serverPubkey(), newConnection.toByteString());

        String pukkey = AllNativeMethod.cdGetPubKeyFromPrivKey(priKey);
        String signHash = SupportKeyUril.signHash(priKey, gcmData.toByteArray());
        Connect.IMRequest imRequest = Connect.IMRequest.newBuilder().
                setSign(signHash).
                setPubKey(pukkey).
                setCipherData(gcmData).build();

        ChatSendManager.getInstance().sendToMsg(SocketACK.HAND_SHAKE_FIRST, imRequest.toByteString());
    }
}
