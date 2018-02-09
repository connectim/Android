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
    public void singleChat(Connect.MessageData messageData) throws Exception {
        getMessageListener().singleChat(messageData);
    }

    @Override
    public void groupChat(Connect.ChatMessage chatMessage) {
        getMessageListener().groupChat(chatMessage);
    }
}
