package instant.parser.localreceiver;

import instant.parser.inter.MessageListener;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/9.
 */

public class MessageLocalReceiver implements MessageListener {

    public static MessageLocalReceiver localReceiver=getInstance();

    private synchronized static MessageLocalReceiver getInstance(){
        if(localReceiver==null){
            localReceiver=new MessageLocalReceiver();
        }
        return localReceiver;
    }

    private MessageListener messageListener=null;

    public void registerMessageListener(MessageListener listener){
        this.messageListener=listener;
    }

    public MessageListener getMessageListener(){
        if(messageListener==null){
            throw new RuntimeException("messageListener don't registe");
        }
        return messageListener;
    }

    @Override
    public long chatBurnTime(String publicKey) {
        return getMessageListener().chatBurnTime(publicKey);
    }

    @Override
    public void singleChat(Connect.ChatMessage chatMessage, String publicKey, byte[] contents) throws Exception {
        getMessageListener().singleChat(chatMessage, publicKey, contents);
    }

    @Override
    public void groupChat(Connect.MessagePost messagePost) {
        getMessageListener().groupChat(messagePost);
    }

    @Override
    public void inviteJoinGroup(Connect.CreateGroupMessage groupMessage) {
        getMessageListener().inviteJoinGroup(groupMessage);
    }
}
