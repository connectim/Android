package instant.parser.inter;

import protos.Connect;

/**
 * SDK 解析具体的 分发的pb到APP  ，该接口在APP解析中实现 处理数据库及消息分发
 * Created by puin on 17-10-8.
 */

public interface CommandListener {

    void commandReceipt(boolean isSuccess,Object reqObj,Object serviceObj);

    void updateMsgSendState(String publickey,String msgid, int state);

    void loadAllContacts(Connect.SyncUserRelationship userRelationship) throws Exception;

    void contactChanges(Connect.ChangeRecords changeRecords);

    void receiverFriendRequest(Connect.ReceiveFriendRequest friendRequest);

    void acceptFriendRequest(Connect.FriendListChange listChange);

    void acceptDelFriend(Connect.FriendListChange listChange);

    void conversationMute(Connect.ManageSession manageSession);

    void updateGroupChange( Connect.GroupChange groupChange) throws Exception;

    void handlerOuterRedPacket(Connect.ExternalRedPackageInfo packageInfo);

    void burnReadingSetting(Connect.EphemeralSetting setting);

    void burnReadingReceipt(Connect.EphemeralAck ack);
}
