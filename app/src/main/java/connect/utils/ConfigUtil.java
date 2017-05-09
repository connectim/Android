package connect.utils;

import android.text.TextUtils;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import connect.ui.base.BaseApplication;
import connect.utils.log.LogManager;

/**
 * Created by gtq on 2016/12/26.
 */
public class ConfigUtil {
    private static ConfigUtil configUtil;

    private String Tag = "ConfigUtil";
    private Map<String, String> keyMaps = null;

    public ConfigUtil() {
        initConfig(ModeEnum.RELEASE);
    }

    /**
     * Initialize the configuration file
     *
     * @param mode test/sandbox/release
     */
    public void initConfig(ModeEnum mode) {
        try {
            keyMaps = new HashMap<>();
            xmlParser(mode.getMode());
        } catch (Exception ex) {
            System.out.println("file is not exist");
        }
    }

    public void xmlParser(String mode) throws XmlPullParserException, IOException {
        InputStream inputStream = BaseApplication.getInstance().getApplicationContext().getAssets().open("config.xml");

        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(inputStream, "UTF-8");

        boolean findMode = false;
        String nameTxt = "";
        String contentTxt = "";
        int eventType = pullParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    keyMaps.clear();
                    break;
                case XmlPullParser.START_TAG:
                    nameTxt = pullParser.getName();
                    eventType = pullParser.next();
                    contentTxt = pullParser.getText();
                    if (TextUtils.isEmpty(nameTxt) || TextUtils.isEmpty(contentTxt)) {
                        break;
                    }

                    if (nameTxt.equals("APP_MODE")) {
                        if (contentTxt.equals("" + mode)) {
                            findMode = true;
                            LogManager.getLogger().d(Tag, "APP_MODE find mode");
                        } else {
                            findMode = false;
                        }
                    }

                    if (findMode) {
                        LogManager.getLogger().d(Tag, nameTxt + "-->" + contentTxt);
                        keyMaps.put(nameTxt, contentTxt);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
            }
            eventType = pullParser.next();
        }
    }

    public static ConfigUtil getInstance() {
        if (configUtil == null) {
            synchronized (ConfigUtil.class) {
                if (configUtil == null) {
                    configUtil = new ConfigUtil();
                }
            }
        }
        return configUtil;
    }

    /**********************************************************************************************
     * Read the different attribute values
     *********************************************************************************************/
    private String APP_MODE = "APP_MODE";
    private String HTTP_MODE = "HTTP_MODE";
    private String SERVER_PUBKEY = "SERVER_PUBKEY";
    private String SERVER_ADDRESS = "SERVER_ADDRESS";
    private String SOCKET_ADDRESS = "SOCKET_ADDRESS";
    private String SOCKET_PORT = "SOCKET_PORT";
    private String SHARE_CARD_ADDRESS = "SHARE_CARD_ADDRESS";
    private String SHARE_PAY_ADDRESS = "SHARE_PAY_ADDRESS";
    private String CRASH_TAGS = "CRASH_TAGS";

    /**
     * app version
     *
     * @return
     */
    public boolean appMode() {
        String mode = keyMaps.get(APP_MODE);
        return "release".equals(mode);
    }

    /**
     * http request mode
     *
     * @return
     */
    public boolean httpMode() {
        String mode = keyMaps.get(HTTP_MODE);
        return "true".equals(mode);
    }

    public String serverPubkey() {
        return keyMaps.get(SERVER_PUBKEY);
    }

    /**
     * Server address
     *
     * @return
     */
    public String serverAddress() {
        return keyMaps.get(SERVER_ADDRESS);
    }

    /**
     * Postal address
     *
     * @return
     */
    public String socketAddress() {
        return keyMaps.get(SOCKET_ADDRESS);
    }

    /**
     * Communication port
     *
     * @return
     */
    public int socketPort() {
        String port = keyMaps.get(SOCKET_PORT);
        return Integer.parseInt(port);
    }

    /**
     * Share card address
     *
     * @return
     */
    public String shareCardAddress() {
        return keyMaps.get(SHARE_CARD_ADDRESS);
    }

    /**
     * Share pay address
     *
     * @return
     */
    public String sharePayAddress() {
        return keyMaps.get(SHARE_PAY_ADDRESS);
    }

    /**
     * bugly Tag
     *
     * @return
     */
    public String getCrashTags() {
        return keyMaps.get(CRASH_TAGS);
    }

    public enum ModeEnum {
        TEST("test"),
        SANDBOX("sandbox"),
        RELEASE("release");

        private String string;
        ModeEnum(String str) {
            this.string = str;
        }

        public String getMode(){
            return string;
        }
    }
}
