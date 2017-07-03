package connect.ui.activity.set.manager;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import connect.utils.RegularUtil;

/**
 *
 * Created by Administrator on 2016/4/28.
 */
public class EditInputFilterPrice implements InputFilter {

    private double maxValue = 1000;
    private int pontintLength = 2;
    Pattern p;
    public EditInputFilterPrice(){
        p = Pattern.compile("[0-9]*");
    }

    public EditInputFilterPrice(Double maxValue, int pontintLength){
        p = Pattern.compile("[0-9]*");
        this.maxValue = maxValue;
        this.pontintLength = pontintLength;
    }

    @Override
    public CharSequence filter(CharSequence src, int start, int end,
                               Spanned dest, int dstart, int dend) {
        String oldtext =  dest.toString();
        System.out.println(oldtext);
        if ("".equals(src.toString())) {
            return null;
        }
        Matcher m = p.matcher(src);
        if(oldtext.contains(".")){
            if(!m.matches()){
                return null;
            }
        }else{
            if(!m.matches() && !src.equals(".") ){
                return null;
            }
        }
        if(!src.toString().equals("") && RegularUtil.matches(oldtext, RegularUtil.VERIFICATION_AMOUT)) {
            if((oldtext+src.toString()).equals(".")){
                return "";
            }
            StringBuffer oldStr = new StringBuffer(oldtext);
            oldStr.insert(dstart,src + "");
            double dold = Double.parseDouble(oldStr.toString());
            if(dold > maxValue){
                return dest.subSequence(dstart, dend);
            }else if(dold == maxValue){
                if(src.toString().equals(".")){
                    return dest.subSequence(dstart, dend);
                }
            }
        }
        if(oldtext.contains(".")){
            int index = oldtext.indexOf(".");
            int len = oldtext.length() - 1 - index;
            if(index < dstart){
                len ++;
            }

            if(len > pontintLength){
                CharSequence newText = dest.subSequence(dstart, dend);
                return newText;
            }
        }
        return dest.subSequence(dstart, dend) +src.toString();
    }

    public void setMaxValue(int maxValue) {
        this.maxValue = maxValue;
    }

    public void setPontintLength(int pontintLength) {
        this.pontintLength = pontintLength;
    }
}
