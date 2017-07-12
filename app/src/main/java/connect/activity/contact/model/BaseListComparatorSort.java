package connect.activity.contact.model;

import android.text.TextUtils;

import connect.utils.PinyinUtil;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * List ordering comparator
 * @param <T>
 */
public abstract class BaseListComparatorSort<T> implements Comparator<T> {

    private Collator collator = Collator.getInstance();

    @Override
    public int compare(T lhs, T rhs) {
        String ostr1;
        String ostr2;
        String lhsName = getName(lhs);
        String rhskName = getName(rhs);

        if (TextUtils.isEmpty(lhsName)) {
            ostr1 = PinyinUtil.getFirstChar(TextUtils.isEmpty(lhsName) ? "#" : lhsName);
        } else {
            ostr1 = PinyinUtil.getFirstChar(TextUtils.isEmpty(lhsName) ? "#" : lhsName);
        }


        if (TextUtils.isEmpty(rhskName)) {
            ostr2 = PinyinUtil.getFirstChar(TextUtils.isEmpty(rhskName) ? "#" : rhskName);
        } else {
            ostr2 = PinyinUtil.getFirstChar(TextUtils.isEmpty(rhskName) ? "#" : rhskName);
        }

        CollationKey key1 = collator.getCollationKey(pinyin(ostr1.equals("") ? '#': ostr1.charAt(0)));
        CollationKey key2 = collator.getCollationKey(pinyin(ostr2.equals("") ? '#': ostr2.charAt(0)));
        // Comparison method violates its general contract
        if(key1.getSourceString().equals(key2.getSourceString())){
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

    /**
     * first char pinyin
     *
     * @param c
     * @return
     */
    private String pinyin(char c) {
        String index = "#";
        if (isChinese(String.valueOf(c))) {
            String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c);
            if (pinyins == null || 0 == pinyins.length) {
                return "#";
            }
            index = pinyins[0];
        } else if (isEnglish(String.valueOf(c))) {
            index = String.valueOf(c);
        } else {
            index = "#";
        }
        return index;
    }

    public boolean isChinese(CharSequence str) {
        Pattern p = Pattern.compile("[\\u4e00-\\u9fa5]");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    public boolean isEnglish(String str) {
        return str.toUpperCase().matches("[a-zA-Z]");
    }

    public abstract String getName(T t);

}
