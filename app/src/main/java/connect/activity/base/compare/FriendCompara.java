package connect.activity.base.compare;

import android.text.TextUtils;

import connect.database.green.bean.ContactEntity;

/**
 * Created by gtq on 2016/12/13.
 */
public class FriendCompara extends BaseCompare<ContactEntity> {

    @Override
    public int compare(ContactEntity lhs, ContactEntity rhs) {
        String lhsStr = lhs.getName();
        String rhsStr = rhs.getName();

        return compareString(lhsStr, rhsStr);
    }
}
