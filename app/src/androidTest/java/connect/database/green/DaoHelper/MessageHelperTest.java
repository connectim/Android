package connect.database.green.DaoHelper;

import org.junit.Test;

import java.util.List;

import connect.utils.log.LogManager;
import instant.bean.ChatMsgEntity;

/**
 * Created by Administrator on 2017/10/24.
 */

public class MessageHelperTest {

    private static String Tag="_MessageHelperTest";

    @Test
    public List<ChatMsgEntity> loadMoreMsgEntities(String message_ower, long firsttime) {
        List<ChatMsgEntity> msgEntities = MessageHelper.getInstance().loadMoreMsgEntities(message_ower, firsttime);

        LogManager.getLogger().d(Tag, "loadMoreMsgEntities: length" + msgEntities.size());
        return msgEntities;
    }
}
