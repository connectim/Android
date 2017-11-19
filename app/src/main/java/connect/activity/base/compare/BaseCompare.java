package connect.activity.base.compare;

import android.text.TextUtils;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by puin on 17-11-20.
 */

public abstract class BaseCompare<T> implements Comparator<T> {

    private Collator collator = Collator.getInstance();

    int compareString(String lhs, String rhs) {
        // if someone is null
        if (TextUtils.isEmpty(lhs) || TextUtils.isEmpty(rhs)) {
            if (TextUtils.isEmpty(lhs) && TextUtils.isEmpty(rhs)) {
                return 0;
            } else if (TextUtils.isEmpty(lhs)) {
                return -1;
            } else if (TextUtils.isEmpty(rhs)) {
                return 1;
            }
        }


        char lhsChar;
        if (TextUtils.isEmpty(lhs)) {
            lhsChar = '#';
        } else if (isEnglish(String.valueOf(lhs.charAt(0)))) {
            lhsChar = lhs.charAt(0);
        } else if (isChinese(String.valueOf(lhs.charAt(0)))) {
            String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(lhs.charAt(0));
            if (pinyins == null || 0 == pinyins.length) {
                lhsChar = '#';
            } else {
                lhsChar = pinyins[0].toUpperCase().charAt(0);
            }
        } else {
            lhsChar = '#';
        }

        char rhsChar;
        if (TextUtils.isEmpty(rhs)) {
            rhsChar = '#';
        } else if (isEnglish(String.valueOf(rhs.charAt(0)))) {
            rhsChar = rhs.charAt(0);
        } else if (isChinese(String.valueOf(lhs.charAt(0)))) {
            String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(rhs.charAt(0));
            if (pinyins == null || 0 == pinyins.length) {
                rhsChar = '#';
            } else {
                rhsChar = pinyins[0].toUpperCase().charAt(0);
            }
        } else {
            rhsChar = '#';
        }

        // someone is #
        if (lhsChar == '#' || rhsChar == '#') {
            if (lhsChar == '#' && rhsChar == '#') {
                CollationKey key1 = collator.getCollationKey(lhs);
                CollationKey key2 = collator.getCollationKey(rhs);
                if (key1.getSourceString().equals(key2.getSourceString())) {
                    return 0;
                } else {
                    return key1.compareTo(key2);
                }
            } else if (lhsChar == '#') {
                return 1;
            } else {
                return -1;
            }
        }


        String lhsComString = toPinYinString(lhs);
        String rhsComString = toPinYinString(rhs);
        CollationKey lhsKey = collator.getCollationKey(lhsComString);
        CollationKey rhsKey = collator.getCollationKey(rhsComString);
        if (lhsKey.getSourceString().equals(rhsKey.getSourceString())) {
            return 0;
        }
        return lhsKey.compareTo(rhsKey);
    }

    public String toPinYinString(String string) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            String charString = String.valueOf(string.charAt(i));
            if (isEnglish(charString)) {
                buffer.append(charString.toUpperCase());
            } else if (isChinese(charString)) {
                String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(string.charAt(i));
                if (pinyins == null || 0 == pinyins.length) {
                    return "#";
                }

                StringBuffer pinyinBuffer = new StringBuffer();
                for (String pinyin : pinyins) {
                    pinyinBuffer.append(pinyin);
                }
                buffer.append(pinyinBuffer);
            } else {
                buffer.append(charString.toUpperCase());
            }
        }
        return buffer.toString();
    }

    public String chatToPinyin(char c) {
        String index = "#";
        if (isChinese(String.valueOf(c))) {
            String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(c);
            if (pinyins == null || 0 == pinyins.length) {
                return "#";
            }
            index = String.valueOf(pinyins[0].toUpperCase().charAt(0));
        } else if (isEnglish(String.valueOf(c))) {
            index = String.valueOf(c).toUpperCase();
        } else {
            index = "#";
        }
        return index;
    }

    /**
     * is chiness
     *
     * @param str
     * @return
     */
    public boolean isChinese(CharSequence str) {
        Pattern p = Pattern.compile("[\\u4e00-\\u9fa5]");
        Matcher m = p.matcher(str);
        return m.matches();
    }

    /**
     * is english
     *
     * @param str
     * @return
     */
    public boolean isEnglish(String str) {
        return str.toUpperCase().matches("[a-zA-Z]");
    }

    /**
     * get first char of string
     *
     * @param value
     * @return
     */
    public String getFirstChar(String value) {
        if (null == value) {
            return "A";
        } else if (TextUtils.isEmpty(value)) {
            return "#";
        }

        char firstChar = value.charAt(0);
        String first = null;
        String[] print = PinyinHelper.toHanyuPinyinStringArray(firstChar);

        if (print == null) {

            if ((firstChar >= 97 && firstChar <= 122)) {
                firstChar -= 32;
            }
            if (firstChar >= 65 && firstChar <= 90) {
                first = String.valueOf(firstChar);
            } else {
                if ("*".equals(String.valueOf(firstChar))) {
                    first = "*";
                } else {
                    first = "#";
                }
            }
        } else {
            first = String.valueOf((char) (print[0].charAt(0) - 32));
        }
        if (first == null) {
            first = "?";
        }
        return first;
    }
}
