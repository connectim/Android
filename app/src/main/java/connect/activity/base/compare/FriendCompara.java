package connect.activity.base.compare;

import android.text.TextUtils;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;

import connect.database.green.bean.ContactEntity;
import connect.utils.PinyinUtil;

/**
 * Created by gtq on 2016/12/13.
 */
public class FriendCompara implements Comparator<ContactEntity> {

    private Collator collator = Collator.getInstance();

    @Override
    public int compare(ContactEntity lhs, ContactEntity rhs) {
        String string1 = TextUtils.isEmpty(lhs.getRemark()) ? lhs.getUsername() : lhs.getRemark();
        String string2 = TextUtils.isEmpty(rhs.getRemark()) ? rhs.getUsername() : rhs.getRemark();

        char char1 = TextUtils.isEmpty(string1) ? '#' : string1.charAt(0);
        char char2 = TextUtils.isEmpty(string2) ? '#' : string2.charAt(0);

        CollationKey key1 = collator.getCollationKey(PinyinUtil.chatToPinyin(char1));
        CollationKey key2 = collator.getCollationKey(PinyinUtil.chatToPinyin(char2));

        // Comparison method violates its general contract
        if (key1.getSourceString().equals(key2.getSourceString())) {
            return 0;
        }
        if ("#".equals(key1.getSourceString())
                || "#".equals(key2.getSourceString())) {
            if ("#".equals(key1.getSourceString())) {
                return 1;
            } else if ("#".equals(key2.getSourceString())) {
                return -1;
            }
        }
        return key1.compareTo(key2);
    }
}
