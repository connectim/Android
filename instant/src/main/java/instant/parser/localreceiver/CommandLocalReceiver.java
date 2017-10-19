package instant.parser.localreceiver;

import instant.parser.inter.CommandListener;
import protos.Connect;

/**
 * Created by puin on 17-10-8.
 */

public class CommandLocalReceiver implements CommandListener {

    public static CommandLocalReceiver receiver = getInstance();

    private synchronized static CommandLocalReceiver getInstance() {
        if (receiver == null) {
            receiver = new CommandLocalReceiver();
        }
        return receiver;
    }

    private CommandListener commandListener = null;

    public void registerCommand(CommandListener listener) {
        this.commandListener = listener;
    }

    public CommandListener getCommandListener() {
        if (commandListener == null) {
            throw new RuntimeException("commandListener don't register");
        }
        return commandListener;
    }

    @Override
    public void updateMsgSendState(String publickey, String msgid, int state) {
        getCommandListener().updateMsgSendState(publickey, msgid, state);
    }

    @Override
    public void commandReceipt(boolean isSuccess, Object reqObj, Object serviceObj) {
        getCommandListener().commandReceipt(isSuccess, reqObj, serviceObj);
    }

    @Override
    public void pullContacts(Connect.SyncUserRelationship userRelationship) throws Exception {
        getCommandListener().pullContacts(userRelationship);
    }

    @Override
    public void contactChanges(Connect.ChangeRecords changeRecords) {
        getCommandListener().contactChanges(changeRecords);
    }

    @Override
    public void receiverFriendRequest(Connect.ReceiveFriendRequest friendRequest) {
        getCommandListener().receiverFriendRequest(friendRequest);
    }

    @Override
    public void acceptFriendRequest(Connect.ReceiveAcceptFriendRequest friendRequest) {
        getCommandListener().acceptFriendRequest(friendRequest);
    }

    @Override
    public void acceptDelFriend(Connect.SyncRelationship relationship) {
        getCommandListener().acceptDelFriend(relationship);
    }

    @Override
    public void conversationMute(Connect.ManageSession manageSession) {
        getCommandListener().conversationMute(manageSession);
    }

    @Override
    public void updateGroupChange(Connect.GroupChange groupChange) throws Exception {
        getCommandListener().updateGroupChange(groupChange);
    }

    @Override
    public void handlerOuterRedPacket(Connect.ExternalRedPackageInfo packageInfo) {
        getCommandListener().handlerOuterRedPacket(packageInfo);
    }

    @Override
    public long chatBurnTime() {
        return getCommandListener().chatBurnTime();
    }

    @Override
    public void homeExit() {
        getCommandListener().homeExit();
    }
}
