package connect.ui.activity.set.manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import connect.ui.activity.wallet.bean.RateBean;

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

}
