package connect.activity.base.compare;

import android.text.TextUtils;

import connect.database.green.bean.GroupMemberEntity;
import connect.utils.log.LogManager;

/**
 * Created by pujin on 2017/2/17.
 */

public class GroupComPara extends BaseCompare<GroupMemberEntity> {

    private static String Tag = "_GroupComPara";

    @Override
    public int compare(GroupMemberEntity lhs, GroupMemberEntity rhs) {
        String lhsStr = TextUtils.isEmpty(lhs.getUsername()) ? "" : lhs.getUsername();
        String rhsStr = TextUtils.isEmpty(rhs.getUsername()) ? "" : rhs.getUsername();
        int compare = compareString(lhsStr, rhsStr);

        LogManager.getLogger().d(Tag, "lhsStr: " + lhsStr + ";  rhsStr:" + rhsStr + "; compare:" + compare);
        return compare;
    }
}
