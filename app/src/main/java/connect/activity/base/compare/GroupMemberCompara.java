package connect.activity.base.compare;

import connect.database.green.bean.GroupMemberEntity;

/**
 * Created by gtq on 2016/12/23.
 */
public class GroupMemberCompara extends BaseCompare<GroupMemberEntity> {

    @Override
    public int compare(GroupMemberEntity lhs, GroupMemberEntity rhs) {
        String lhsStr = lhs.getUsername();
        String rhsStr = rhs.getUsername();
        return compareString(lhsStr, rhsStr);
    }
}