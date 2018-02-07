package connect.database.green.DaoHelper;

import org.junit.Test;

import java.util.List;

import connect.activity.chat.bean.Talker;
import connect.database.green.bean.GroupMemberEntity;
import connect.utils.log.LogManager;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/24.
 */

public class ContactHelperTest {

    private String Tag = "_ContactHelperTest";

    @Test
    public Connect.ChatType loadChatType(String uid) {
        return ContactHelper.getInstance().loadChatType(uid);
    }

    @Test
    public Talker loadTalkerGroup(String identify) {
        return ContactHelper.getInstance().loadTalkerGroup(identify);
    }

    @Test
    public List<GroupMemberEntity> loadGroupMemEntities(String pukkey) {
        List<GroupMemberEntity> memberEntities = ContactHelper.getInstance().loadGroupMemEntities(pukkey);

        LogManager.getLogger().d(Tag, "loadGroupMemEntities: length" + memberEntities.size());
        return memberEntities;
    }

    @Test
    public List<GroupMemberEntity> loadGroupMemEntities(String identify,String memberkey) {
        List<GroupMemberEntity> memberEntities = ContactHelper.getInstance().loadGroupMemEntities(identify,memberkey);

        LogManager.getLogger().d(Tag, "loadGroupMemEntities: length" + memberEntities.size());
        return memberEntities;
    }
}
