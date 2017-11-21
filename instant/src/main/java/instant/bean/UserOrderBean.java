package instant.bean;

import com.google.protobuf.ByteString;

import instant.parser.InterParse;
import instant.utils.TimeUtil;
import instant.utils.manager.FailMsgsManager;
import protos.Connect;

/**
 * Created by pujin on 2017/5/16.
 */

public class UserOrderBean extends InterParse {

    @Override
    public void msgParse() throws Exception {

    }

    /**
     * remove friend
     *
     * @param objects
     */
    public void removeRelation(Object... objects) {
        Connect.RemoveRelationship removeRelation = Connect.RemoveRelationship.newBuilder()
                .setUid((String) objects[0]).build();

        String msgid = TimeUtil.timestampToMsgid();
        if (objects.length == 2) {
            FailMsgsManager.getInstance().insertFailMsg("", msgid, null, null, objects[1]);
        }
        commandToIMTransfer(msgid, SocketACK.REMOVE_FRIEND, removeRelation.toByteString());
    }

    /**
     * Request to add friends
     *
     * @param objects
     * @return
     */
    public void requestAddFriend(Object... objects) {
        Connect.AddFriendRequest addFriend = Connect.AddFriendRequest.newBuilder()
                .setUid((String) objects[0])
                .setTips(((String) objects[2]))
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
    public void acceptFriendRequest(Object... objects) {
        Connect.AcceptFriendRequest friendRequest = Connect.AcceptFriendRequest.newBuilder()
                .setUid((String) objects[0])
                .setSource((Integer) objects[1])
                .build();

        String msgid = TimeUtil.timestampToMsgid();
        if (objects.length == 3) {
            FailMsgsManager.getInstance().insertFailMsg("", msgid, null, null, objects[2]);
        }
        commandToIMTransfer(msgid, SocketACK.AGREE_FRIEND, friendRequest.toByteString());
    }

    /**
     * Modify the friends setting
     *
     * @param category:
     *                  "COMMON":     "common",
     *                  "COMMON_DEL": "common_del",
     *                  "BLACK":      "black",
     *                  "BLACK_DEL":  "black_del",
     *                  "REMARK":     "remark",
     * @param uid
     */
    public void settingFriend(String uid, String category, boolean common, String remark, Object obj) {
        Connect.SettingFriendInfo friendInfo = Connect.SettingFriendInfo.newBuilder()
                .setUid(uid)
                .setCategory(category)
                .setCommon(common)
                .setRemark(remark)
                .build();

        String msgid = TimeUtil.timestampToMsgid();
        if (obj != null) {
            FailMsgsManager.getInstance().insertFailMsg("", msgid, null, null, obj);
        }
        commandToIMTransfer(msgid, SocketACK.SET_FRIEND, friendInfo.toByteString());
    }

    /**
     * outer transaction
     * @param objects
     */
    public void outerTransfer(Object... objects) {
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
    public void outerRedPacket(Object... objects) {
        Connect.RedPackageToken billingToken = Connect.RedPackageToken.newBuilder()
                .setToken((String) objects[0]).build();

        String msgid = TimeUtil.timestampToMsgid();
        if (objects.length == 2) {
            FailMsgsManager.getInstance().insertFailMsg("", msgid, SocketACK.OUTER_REDPACKET,
                    Connect.Command.newBuilder().setMsgId(msgid).
                            setDetail(billingToken.toByteString()).build().toByteString(),
                    objects[1]);
        }
        commandToIMTransfer(msgid, SocketACK.OUTER_REDPACKET, billingToken.toByteString());
    }

    /**
     * upload  Cookie
     *
     * @param cookie
     */
    public void uploadRandomCookie(Connect.ChatCookie cookie) {
        String msgid = TimeUtil.timestampToMsgid();
        commandToIMTransfer(msgid, SocketACK.UPLOAD_CHATCOOKIE, cookie.toByteString());
    }

    /**
     * get friend chatcookie
     */
    public void friendChatCookie(String friendUid) {
        String msgid = TimeUtil.timestampToMsgid();
        Connect.FriendChatCookie chatInfo = Connect.FriendChatCookie.newBuilder().
                setUid(friendUid)
                .build();

        commandToIMTransfer(msgid, SocketACK.DOWNLOAD_FRIENDCOOKIE, chatInfo.toByteString());
        FailMsgsManager.getInstance().insertFailMsg("", msgid, null, null, friendUid);
    }

    /**
     * login out
     */
    public void connectLogout() {
        String msgid = TimeUtil.timestampToMsgid();
        commandToIMTransfer(msgid, SocketACK.CONTACT_LOGOUT, ByteString.copyFrom(new byte[]{}));
    }

    /**
     * 开启阅后即焚时间
     *
     * @param uid
     * @param friendUid
     * @param time 开始时间
     * @return
     */
    public void burnReadSetting(String uid, String friendUid, int time) {
        Connect.EphemeralSetting setting = Connect.EphemeralSetting.newBuilder()
                .setUid(uid)
                .setFriendUid(friendUid)
                .setDeadline(time)
                .build();
        String msgid = TimeUtil.timestampToMsgid();
        commandToIMTransfer(msgid, SocketACK.BURNREAD_SETTING, setting.toByteString());
    }

    /**
     * 阅后即焚消息已读
     *
     * @param uid
     * @param friendUid
     * @param messageid 已读消息id
     * @return
     */
    public void burnReadReceipt(String uid, String friendUid, String messageid) {
        Connect.EphemeralAck setting = Connect.EphemeralAck.newBuilder()
                .setUid(uid)
                .setFriendUid(friendUid)
                .setMsgID(messageid)
                .build();
        String msgid = TimeUtil.timestampToMsgid();
        commandToIMTransfer(msgid, SocketACK.BURNREAD_RECEIPT, setting.toByteString());
    }
}
