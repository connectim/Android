package connect.activity.base.compare;

import android.text.TextUtils;

import connect.database.green.bean.ContactEntity;

/**
 * Created by gtq on 2016/12/13.
 */
public class FriendCompara extends BaseCompare<ContactEntity> {

    @Override
    public int compare(ContactEntity lhs, ContactEntity rhs) {
        String lhsStr = TextUtils.isEmpty(lhs.getRemark()) ? lhs.getUsername() : lhs.getRemark();
        String rhsStr = TextUtils.isEmpty(rhs.getRemark()) ? rhs.getUsername() : rhs.getRemark();

        return compareString(lhsStr, rhsStr);
    }
}
