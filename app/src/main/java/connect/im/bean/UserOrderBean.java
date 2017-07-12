package connect.im.bean;

import com.google.protobuf.ByteString;

import java.io.UnsupportedEncodingException;

import connect.database.MemoryDataManager;
import connect.im.inter.InterParse;
import connect.im.model.FailMsgsManager;
import connect.utils.TimeUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import protos.Connect;

/**
 * Created by pujin on 2017/5/16.
 */

public class UserOrderBean extends InterParse {

    @Override
    public void msgParse() throws Exception {

    }

    /**
     * Request to add friends
     *
     * @param objects
     * @return
     */
    public void requestAddFriend(Object... objects) {
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
    public void acceptFriendRequest(Object... objects) {
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
    public void removeRelation(Object... objects) {
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
    public  void noInterested(Object... objects) {
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
    public void setFriend(Object... objects) {
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
    public void friendChatCookie(String pubkey) {
        String msgid = TimeUtil.timestampToMsgid();
        Connect.FriendChatCookie chatInfo = Connect.FriendChatCookie.newBuilder().
                setAddress(SupportKeyUril.getAddressFromPubkey(pubkey)).build();

        commandToIMTransfer(msgid, SocketACK.DOWNLOAD_FRIENDCOOKIE, chatInfo.toByteString());

        FailMsgsManager.getInstance().insertFailMsg("", msgid, null,null,pubkey);
    }

    /**
     * login out
     */
    public void connectLogout() {
        String msgid = TimeUtil.timestampToMsgid();
        commandToIMTransfer(msgid, SocketACK.CONTACT_LOGOUT, ByteString.copyFrom(new byte[]{}));
    }
}
