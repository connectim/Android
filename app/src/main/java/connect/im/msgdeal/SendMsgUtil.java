package connect.im.msgdeal;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import com.google.protobuf.ByteString;

import java.io.UnsupportedEncodingException;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.ParamHelper;
import connect.db.green.DaoHelper.ParamManager;
import connect.db.green.bean.ParamEntity;
import connect.im.bean.ConnectState;
import connect.im.bean.Session;
import connect.im.bean.SocketACK;
import connect.im.bean.UserCookie;
import connect.im.model.ChatSendManager;
import connect.im.model.FailMsgsManager;
import connect.ui.base.BaseApplication;
import connect.utils.ConfigUtil;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.system.SystemDataUtil;
import connect.wallet.jni.AllNativeMethod;
import protos.Connect;

/**
 * Created by gtq on 2016/11/30.
 */
public class SendMsgUtil {

    private static String Tag = "SendMsgUtil";

    /**
     * A shake hands for the first time
     *
     * @return
     */
    public static Connect.IMRequest firstLoginShake() {
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
        return Connect.IMRequest.newBuilder().
                setSign(signHash).
                setPubKey(pukkey).
                setCipherData(gcmData).build();
    }

    /**
     * login
     */
    public static void connectLogin() {
        String deviceId = SystemDataUtil.getDeviceId();
        Connect.DeviceToken deviceToken = Connect.DeviceToken.newBuilder()
                .setDeviceId(deviceId)
                .setPushType("GCM").build();

        String msgid = TimeUtil.timestampToMsgid();
        commandToIMTransfer(msgid, SocketACK.CONTACT_LOGIN, deviceToken.toByteString());
    }

    /**
     * login out
     */
    public static void connectLogout() {
        String msgid = TimeUtil.timestampToMsgid();
        commandToIMTransfer(msgid, SocketACK.CONTACT_LOGOUT, ByteString.copyFrom(new byte[]{}));
    }

    private static void commandToIMTransfer(String msgid, SocketACK ack, ByteString byteString) {
        Connect.Command command = Connect.Command.newBuilder().setMsgId(msgid).
                setDetail(byteString).build();
        ChatSendManager.getInstance().sendMsgidMsg(ack, msgid, command.toByteString());
    }

    /**
     * sycn contact
     *
     * @return
     */
    public static void requestFriendsByVersion() {
        String version = ParamManager.getInstance().getString(ParamManager.COUNT_FRIENDLIST);
        if (TextUtils.isEmpty(version)) {
            version = "0";
        }
        Connect.SyncRelationship syncRelationship = Connect.SyncRelationship.newBuilder()
                .setVersion(version).build();
        commandToIMTransfer(TimeUtil.timestampToMsgid(), SocketACK.CONTACT_SYNC, syncRelationship.toByteString());
    }

    /**
     * pull offline message
     */
    public static void pullOffLineMsg() {
        String msgid = TimeUtil.timestampToMsgid();
        ChatSendManager.getInstance().sendMsgidMsg(SocketACK.PULL_OFFLINE, msgid, ByteString.copyFrom(new byte[]{}));
    }

    /**
     * current version
     */
    public static void checkCurVersion() {
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
            // can not reach
        }
    }

    /**
     * Request to add friends
     *
     * @param objects
     * @return
     */
    public static void requestAddFriend(Object... objects) {
        String priKey = MemoryDataManager.getInstance().getPriKey();
        Connect.GcmData gcmData = null;
        try {
            gcmData = EncryptionUtil.encodeAESGCM(SupportKeyUril.EcdhExts.NONE, priKey, (String) objects[1], ((String) objects[2]).getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Connect.AddFriendRequest addFriend = Connect.AddFriendRequest.newBuilder()
                .setAddress((String) objects[0])
                .setTips(gcmData)
                .setSource((Integer) objects[3]).build();

        String msgid = TimeUtil.timestampToMsgid();
        if (objects.length == 5) {
            FailMsgsManager.getInstance().insertFailMsg("", msgid, null,null, objects[4]);
        }
        commandToIMTransfer(msgid, SocketACK.ADD_FRIEND, addFriend.toByteString());
    }

    /**
     * Agree to add friends
     *
     * @param objects
     */
    public static void acceptFriendRequest(Object... objects) {
        Connect.AcceptFriendRequest friendRequest = Connect.AcceptFriendRequest.newBuilder()
                .setAddress((String) objects[0])
                .setSource((Integer) objects[1])
                .build();

        String msgid = TimeUtil.timestampToMsgid();
        if (objects.length == 3) {
            FailMsgsManager.getInstance().insertFailMsg("", msgid, null, null, objects[2]);
        }
        commandToIMTransfer(msgid, SocketACK.AGREE_FRIEND, friendRequest.toByteString());
    }

    /**
     * remove friend
     *
     * @param objects
     */
    public static void removeRelation(Object... objects) {
        Connect.RemoveRelationship removeRelation = Connect.RemoveRelationship.newBuilder()
                .setAddress((String) objects[0]).build();

        String msgid = TimeUtil.timestampToMsgid();
        if (objects.length == 2) {
            FailMsgsManager.getInstance().insertFailMsg("", msgid, null, null, objects[1]);
        }
        commandToIMTransfer(msgid, SocketACK.REMOVE_FRIEND, removeRelation.toByteString());
    }

    /**
     * Not interested in
     *
     * @param objects
     */
    public static void noInterested(Object... objects) {
        Connect.NOInterest noInterest = Connect.NOInterest.newBuilder().setAddress((String) objects[0]).build();

        String msgid = TimeUtil.timestampToMsgid();
        if (objects.length == 2) {
            FailMsgsManager.getInstance().insertFailMsg("", msgid, null,null,  objects[1]);
        }
        commandToIMTransfer(msgid, SocketACK.NO_INTERESTED, noInterest.toByteString());
    }

    /**
     * Modify the friends remark and common friends
     *
     * @param objects
     */
    public static void setFriend(Object... objects) {
        Connect.SettingFriendInfo friendInfo = Connect.SettingFriendInfo.newBuilder()
                .setAddress((String) objects[0])
                .setRemark((String) objects[1])
                .setCommon((Boolean) objects[2]).build();

        String msgid = TimeUtil.timestampToMsgid();
        if (objects.length == 4) {
            FailMsgsManager.getInstance().insertFailMsg("", msgid, null,null, objects[3]);
        }
        commandToIMTransfer(msgid, SocketACK.SET_FRIEND, friendInfo.toByteString());
    }

    /**
     * outer transaction
     * @param objects
     */
    public static void outerTransfer(Object... objects) {
        Connect.ExternalBillingToken billingToken = Connect.ExternalBillingToken.newBuilder()
                .setToken((String) objects[0]).build();

        String msgid = TimeUtil.timestampToMsgid();
        if (objects.length == 2) {
            FailMsgsManager.getInstance().insertFailMsg("", msgid, null,null, objects[1]);
        }
        commandToIMTransfer(msgid, SocketACK.OUTER_TRANSFER, billingToken.toByteString());
    }

    /**
     * outer lucky packet
     *
     * @param objects
     */
    public static void outerRedPacket(Object... objects) {
        Connect.RedPackageToken billingToken = Connect.RedPackageToken.newBuilder()
                .setToken((String) objects[0]).build();

        String msgid = TimeUtil.timestampToMsgid();
        if (objects.length == 2) {
            FailMsgsManager.getInstance().insertFailMsg("", msgid, null, null, objects[1]);
        }
        commandToIMTransfer(msgid, SocketACK.OUTER_REDPACKET, billingToken.toByteString());
    }

    /**
     * upload  Cookie
     *
     * @param cookie
     */
    public static void uploadRandomCookie(Connect.ChatCookie cookie) {
        String msgid = TimeUtil.timestampToMsgid();
        commandToIMTransfer(msgid, SocketACK.UPLOAD_CHATCOOKIE, cookie.toByteString());
    }

    /**
     * get friend chatcookie
     */
    public static void friendChatCookie(String pubkey) {
        String msgid = TimeUtil.timestampToMsgid();
        Connect.FriendChatCookie chatInfo = Connect.FriendChatCookie.newBuilder().
                setAddress(SupportKeyUril.getAddressFromPubkey(pubkey)).build();

        commandToIMTransfer(msgid, SocketACK.DOWNLOAD_FRIENDCOOKIE, chatInfo.toByteString());

        FailMsgsManager.getInstance().insertFailMsg("", msgid, null,null,pubkey);
    }
}