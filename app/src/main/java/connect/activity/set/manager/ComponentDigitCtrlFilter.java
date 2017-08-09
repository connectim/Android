package connect.activity.set.manager;

import android.text.InputFilter;
import android.text.Spanned;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * In order to limit the edit according to the goods specified digits and small digital input
 */
public class ComponentDigitCtrlFilter implements InputFilter {

    /**
     * max value
     */
    private double maxValue = 1000;

    private int pontintLength = 2;
    Pattern p;
    public ComponentDigitCtrlFilter(){
        p = Pattern.compile("[0-9]*");
    }

    public ComponentDigitCtrlFilter(double maxValue, int pontintLength){
        p = Pattern.compile("[0-9]*");
        this.maxValue = maxValue;
        this.pontintLength = pontintLength;
    }
    /**
     *  source    The new input string
     *  start    New start index to the string that is input, average is 0
     *  end    The new input string at the end of the subscript, general source in length - 1
     *  dest    Before the input text box
     *  dstart    The original contents starting coordinates, general is 0
     *  dend    The original contents coordinates, generally for the dest length - 1
     */

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
        if(!src.toString().equals("")){
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
            int len = oldtext.length()-1 + end - index;

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