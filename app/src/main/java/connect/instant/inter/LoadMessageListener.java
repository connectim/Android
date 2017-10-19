package connect.instant.inter;

import java.util.List;

import instant.bean.ChatMsgEntity;

/**
 * Created by Administrator on 2017/10/19.
 */
public interface LoadMessageListener {

    List<ChatMsgEntity> loadEntities(long lastMessageTime);

    ChatMsgEntity loadEntityByMsgid(String msgid);
}
