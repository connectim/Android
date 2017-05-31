package connect.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Xml;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessageV3;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import connect.ui.base.BaseApplication;

/**
 * Created by pujin on 2017/5/31.
 */

public class ProtoBufUtil {

    private static ProtoBufUtil protoBufUtil;
    private Map<String, Map<String, Map>> protoBufMap = new HashMap<>();

    public ProtoBufUtil() {
        try {
            xmlParser();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ProtoBufUtil getInstance() {
        if (protoBufUtil == null) {
            synchronized (ProtoBufUtil.class) {
                if (protoBufUtil == null) {
                    protoBufUtil = new ProtoBufUtil();
                }
            }
        }
        return protoBufUtil;
    }

    public void xmlParser() throws XmlPullParserException, IOException {
        Context context = BaseApplication.getInstance().getApplicationContext();
        InputStream inputStream = context.getAssets().open("pb.xml");

        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(inputStream, "UTF-8");

        String pbTxt="";
        String nameTxt = "";
        String extTxt = "";
        String contentTxt = "";
        Map<String,Map> pbMap=null;
        Map attrMap = null;
        int eventType = pullParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    break;
                case XmlPullParser.START_TAG:
                    nameTxt = pullParser.getAttributeValue("", "name");
                    extTxt = pullParser.getAttributeValue("", "ext");
                    if (TextUtils.isEmpty(extTxt)) {
                        pbTxt = nameTxt;
                    } else {
                        eventType = pullParser.next();
                        contentTxt = pullParser.getText();

                        pbMap = protoBufMap.get(pbTxt);
                        if (pbMap == null) {
                            pbMap = new HashMap();
                        }
                        attrMap = pbMap.get(nameTxt);
                        if (attrMap == null) {
                            attrMap = new HashMap();
                        }
                        attrMap.put("EXT", extTxt);
                        attrMap.put("CONTENT", contentTxt);

                        pbMap.put(nameTxt, attrMap);
                        protoBufMap.put(pbTxt, pbMap);
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
            }
            eventType = pullParser.next();
        }
    }

    public boolean checkProtoBuf(GeneratedMessageV3 messageV3) {
        String nameTxt = messageV3.getClass().getSimpleName();
        Map<String, Object> fieldMap = new HashMap<>();
        Map<Descriptors.FieldDescriptor, Object> fieldDesMap = messageV3.getAllFields();
        for (Map.Entry<Descriptors.FieldDescriptor, Object> desc : fieldDesMap.entrySet()) {
            fieldMap.put(desc.getKey().getJsonName(), desc.getValue());
        }

        boolean checkstate = true;
        if (protoBufMap == null) {
            try {
                xmlParser();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        Map<String, Map> attrMap = protoBufMap.get(nameTxt);
        if (attrMap == null) {
            return false;
        }

        for (Map.Entry<String, Map> attr : attrMap.entrySet()) {
            String attrTxt = attr.getKey();
            checkstate = fieldMap.containsKey(attrTxt);
            if (checkstate) {
                Map<String, String> contentMap = attr.getValue();
                String extTxt = contentMap.get("EXT");
                String content = contentMap.get("CONTENT");

                String value = String.valueOf(fieldMap.get(attrTxt));
                switch (extTxt) {
                    case "reg":
                        checkstate = RegularUtil.matches(value, content);
                        break;
                    case "string":
                        break;
                    case "int":
                        break;
                    case "float":
                        break;
                    case "bool":
                        break;
                }
            }
        }
        return checkstate;
    }
}
