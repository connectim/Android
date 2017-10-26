package connect.utils.filter;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import connect.utils.RegularUtil;

/**
 * Filter the size of the input number
 */
public class EditInputFilterPrice implements InputFilter {

    private double maxValue = 1000;
    private int pointIntLength = 2;
    Pattern pattern;

    public EditInputFilterPrice(){
        pattern = Pattern.compile("[0-9]*");
    }

    public EditInputFilterPrice(Double maxValue, int pointIntLength) {
        pattern = Pattern.compile("[0-9]*");
        this.maxValue = maxValue;
        this.pointIntLength = pointIntLength;
    }

    @Override
    public CharSequence filter(CharSequence src, int start, int end, Spanned dest, int dstart, int dend) {
        String oldText =  dest.toString();
        System.out.println(oldText);
        if ("".equals(src.toString())) {
            return null;
        }
        Matcher matcher = pattern.matcher(src);
        if (oldText.contains(".")) {
            if (!matcher.matches()) {
                return null;
            }
        } else {
            if (!matcher.matches() && !src.equals(".")) {
                return null;
            }
        }
        if (!src.toString().equals("") && RegularUtil.matches(oldText, RegularUtil.VERIFICATION_AMOUT)) {
            if ((oldText + src.toString()).equals(".")) {
                return "";
            }
            StringBuffer oldStr = new StringBuffer(oldText);
            oldStr.insert(dstart,src + "");
            double dold = Double.parseDouble(oldStr.toString());
            if (dold > maxValue) {
                return dest.subSequence(dstart, dend);
            } else if (dold == maxValue) {
                if (src.toString().equals(".")) {
                    return dest.subSequence(dstart, dend);
                }
            }
        }
        if (oldText.contains(".")) {
            int index = oldText.indexOf(".");
            int len = oldText.length() - 1 - index;
            if (index < dstart) {
                len ++;
            }

            if (len > pointIntLength) {
                CharSequence newText = dest.subSequence(dstart, dend);
                return newText;
            }
        }
        return dest.subSequence(dstart, dend) + src.toString();
    }

}
