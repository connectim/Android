package connect.activity.base.compare;

import android.text.TextUtils;

import connect.database.green.bean.GroupMemberEntity;

/**
 * Created by gtq on 2016/12/23.
 */
public class GroupMemberCompara extends BaseCompare<GroupMemberEntity> {

    @Override
    public int compare(GroupMemberEntity lhs, GroupMemberEntity rhs) {
        String lhsStr = TextUtils.isEmpty(lhs.getNick()) ? lhs.getUsername() : lhs.getNick();
        String rhsStr = TextUtils.isEmpty(rhs.getNick()) ? rhs.getUsername() : rhs.getNick();

        return compareString(lhsStr, rhsStr);
    }
}