package connect.activity.base.compare;

import android.text.TextUtils;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;

import connect.database.green.bean.GroupMemberEntity;
import connect.utils.PinyinUtil;

/**
 * Created by pujin on 2017/2/17.
 */

public class GroupComPara implements Comparator<GroupMemberEntity> {
    private Collator collator = Collator.getInstance();

    @Override
    public int compare(GroupMemberEntity lhs, GroupMemberEntity rhs) {
        String string1 = TextUtils.isEmpty(lhs.getNick()) ? lhs.getUsername() : lhs.getNick();
        String string2 = TextUtils.isEmpty(rhs.getNick()) ? rhs.getUsername() : rhs.getNick();

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
