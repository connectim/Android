package instant.utils;

import android.text.TextUtils;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import instant.ui.InstantSdk;

/**
 * Created by Administrator on 2017/10/18.
 */

public class XmlParser {
    private static XmlParser xmlParser;

    private Map<String, String> keyMaps = null;

    public XmlParser() {
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

    /**
     * Parse the configuration xml file
     * @param mode
     * @throws XmlPullParserException
     * @throws IOException
     */
    public void xmlParser(String mode) throws XmlPullParserException, IOException {
        InputStream inputStream = InstantSdk.getInstance().getBaseContext().getAssets().open("connect.xml");

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
                        } else {
                            findMode = false;
                        }
                    }

                    if (findMode) {
                        keyMaps.put(nameTxt, contentTxt);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
            }
            eventType = pullParser.next();
        }
    }

    public synchronized static XmlParser getInstance() {
        if (xmlParser == null) {
            xmlParser = new XmlParser();
        }
        return xmlParser;
    }

    /**********************************************************************************************
     * Read the different attribute values
     *********************************************************************************************/
    private String APP_MODE = "APP_MODE";
    private String HTTP_MODE = "HTTP_MODE";
    private String SERVER_ADDRESS = "SERVER_ADDRESS";
    private String SERVER_PUBKEY = "SERVER_PUBKEY";
    private String SOCKET_ADDRESS = "SOCKET_ADDRESS";
    private String SOCKET_PORT = "SOCKET_PORT";
    private String CRASH_TAGS = "CRASH_TAGS";
    private String CRASH_APPID= "CRASH_APPID";

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

    /**
     * Server address
     *
     * @return
     */
    public String serverAddress() {
        return keyMaps.get(SERVER_ADDRESS);
    }

    public String serverPubKey() {
        return keyMaps.get(SERVER_PUBKEY);
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
     * bugly Tag
     *
     * @return
     */
    public String getCrashTags() {
        return keyMaps.get(CRASH_TAGS);
    }

    public String getCrashAPPID(){
        return keyMaps.get(CRASH_APPID);
    }

    public enum ModeEnum {
        PC("pc"),
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
