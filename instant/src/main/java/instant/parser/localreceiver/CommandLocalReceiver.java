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
    public void loadAllContacts(Connect.SyncCompany userRelationship) throws Exception {
        getCommandListener().loadAllContacts(userRelationship);
    }

    @Override
    public void contactChanges(Connect.WorkmateChangeRecords changeRecords) {
        getCommandListener().contactChanges(changeRecords);
    }

    @Override
    public void conversationMute(Connect.ManageSession manageSession) {
        getCommandListener().conversationMute(manageSession);
    }

    @Override
    public void updateGroupChange(Connect.GroupChange groupChange) throws Exception {
        getCommandListener().updateGroupChange(groupChange);
    }
}
