package connect.instant.receiver;

import org.greenrobot.eventbus.EventBus;

import instant.parser.inter.CommandListener;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/10.
 */
public class CommandReceiver implements CommandListener {

    private String Tag = "_CommandReceiver";

    public static CommandReceiver receiver = getInstance();

    private synchronized static CommandReceiver getInstance() {
        if (receiver == null) {
            receiver = new CommandReceiver();
        }
        return receiver;
    }

    @Override
    public void commandReceipt(boolean isSuccess, Object reqObj, Object serviceObj) {

    }

    @Override
    public void updateMsgSendState(String publickey, String msgid, int state) {

    }

    @Override
    public void pullContacts(Connect.SyncUserRelationship userRelationship) throws Exception {
        Connect.RelationShip relationShip = userRelationship.getRelationShip();
        EventBus.getDefault().post(relationShip);
    }

    @Override
    public void contactChanges(Connect.ChangeRecords changeRecords) {

    }

    @Override
    public void receiverFriendRequest(Connect.ReceiveFriendRequest friendRequest) {

    }

    @Override
    public void acceptFriendRequest(Connect.ReceiveAcceptFriendRequest friendRequest) {

    }

    @Override
    public void acceptDelFriend(Connect.SyncRelationship relationship) {

    }

    @Override
    public void conversationMute(Connect.ManageSession manageSession) {

    }

    @Override
    public void updateGroupChange(Connect.GroupChange groupChange) throws Exception {

    }

    @Override
    public void handlerOuterRedPacket(Connect.ExternalRedPackageInfo packageInfo) {

    }

    @Override
    public long chatBurnTime() {
        return 0;
    }

    @Override
    public void homeExit() {

    }
}
