package connect.utils;

import android.text.TextUtils;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by gtq on 2016/12/13.
 */
public class PinyinUtil {

    public static String chatToPinyin(char c) {
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
    public static boolean isChinese(CharSequence str) {
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
    public static boolean isEnglish(String str) {
        return str.toUpperCase().matches("[a-zA-Z]");
    }

    public static String getFirstChar(String value) {
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
