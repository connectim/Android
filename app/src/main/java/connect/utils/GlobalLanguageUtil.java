package connect.utils;

import android.text.TextUtils;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import connect.database.SharedPreferenceUtil;
import connect.activity.base.BaseApplication;

/**
 * Language International Chemical
 */
public class GlobalLanguageUtil {
    private static GlobalLanguageUtil globalLanguageUtil;

    public static GlobalLanguageUtil getInstance() {
        if (globalLanguageUtil == null) {
            synchronized (GlobalLanguageUtil.class) {
                if (globalLanguageUtil == null) {
                    globalLanguageUtil = new GlobalLanguageUtil();
                }
            }
        }
        return globalLanguageUtil;
    }

    private String Tag = "GlobalLanguageUtil";
    private Map<String, String> languageMap = new HashMap<>();

    public GlobalLanguageUtil() {
        String code = SharedPreferenceUtil.getInstance().getStringValue(SharedPreferenceUtil.APP_LANGUAGE_CODE);
        if (TextUtils.isEmpty(code)) {
            code = "en";
        }
        try {
            xmlParse(code);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void transLanguage() {
        globalLanguageUtil = null;
        getInstance();
    }

    public void xmlParse(String code) throws XmlPullParserException, IOException {
        InputStream inputStream = BaseApplication.getInstance().getApplicationContext().getAssets().open("emotion_zh.xml");

        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(inputStream, "UTF-8");

        String keyEn = "";
        String keyZh = "";
        int eventType = pullParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    languageMap.clear();
                    break;
                case XmlPullParser.START_TAG:
                    keyEn = pullParser.getAttributeValue("", "name");
                    pullParser.next();
                    keyZh = pullParser.getText();
                    if (!(TextUtils.isEmpty(keyEn) || TextUtils.isEmpty(keyZh))) {
                        if (code.contains("en")) {//ch to en
                            languageMap.put(keyZh, keyEn);
                        } else {//en to ch
                            languageMap.put(keyEn, keyZh);
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
            }
            eventType = pullParser.next();
        }
    }

    /**
     * To convert a string into an application language
     * @return
     */
    public String translateValue(String string){
        Pattern pattern = Pattern.compile(RegularUtil.VERIFYCATION_EMOTION);
        Matcher matcher = pattern.matcher(string);

        StringBuffer buffer = new StringBuffer();
        int startPosi = 0;
        while (matcher.find()) {
            String group = matcher.group();
            buffer.append(string.substring(startPosi, matcher.start()));
            buffer.append(replaceLanguage(group));
            startPosi = matcher.end();
        }

        int endPosi = string.length();
        if (startPosi != endPosi) {
            buffer.append(string.substring(startPosi, endPosi));
        }
        return buffer.toString();
    }

    public String replaceLanguage(String group) {
        String index = group;
        if (group.length() > 2) {
            index = index.substring(1, index.length() - 1);
        }
        index = languageMap.get(index);
        return TextUtils.isEmpty(index) ? group : "[" + index + "]";
    }
}
