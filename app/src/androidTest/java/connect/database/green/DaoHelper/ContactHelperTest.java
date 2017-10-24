package connect.database.green.DaoHelper;

import org.junit.Test;

import java.util.List;

import connect.database.green.bean.GroupMemberEntity;
import connect.utils.log.LogManager;

/**
 * Created by Administrator on 2017/10/24.
 */

public class ContactHelperTest {

    private String Tag = "_ContactHelperTest";

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

    @Test
    public void updateGroupMember(String identify, String uid, String username, String avatar, Integer role, String nickname) {
        ContactHelper.getInstance().updateGroupMember(identify, uid, username, avatar, role, nickname);
    }
}
