package connect.utils.data;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Locale;

/**
 * The local language data classes
 */

public class LanguageData {

    private static LanguageData languageData = null;

    public static LanguageData getInstance() {
        if (null == languageData) {
            languageData = new LanguageData();
        }
        return languageData;
    }

    public static final String language_data =
            "[\n" +
                    "    {\n" +
                    "        \"code\": \"en\",\n" +
                    "        \"name\": \"English\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"code\": \"ru\",\n" +
                    "        \"name\": \"русский\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "        \"code\": \"zh\",\n" +
                    "        \"name\": \"简体中文\"\n" +
                    "    }\n" +
                    "]";

    /**
     * Access to the local language data
     */
    public ArrayList<RateBean> getLanguageData() {
        Type type = new TypeToken<ArrayList<RateBean>>() {}.getType();
        return new Gson().fromJson(language_data, type);
    }

    public RateBean getLanguageData(String code) {
        if(TextUtils.isEmpty(code)){
            Locale myLocale = Locale.getDefault();
            code = myLocale.getLanguage();
        }
        Type type = new TypeToken<ArrayList<RateBean>>() {}.getType();
        ArrayList<RateBean> list =  new Gson().fromJson(language_data, type);
        for(RateBean rateBean : list){
            if(rateBean.getCode().equals(code)){
                return rateBean;
            }
        }
        return new RateBean();
    }

}
