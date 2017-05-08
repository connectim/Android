package connect.utils.okhttp.adapter;

import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.bean.MsgSender;
import connect.ui.activity.locmap.bean.GeoAddressBean;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

/**
 * parse MsgDefinBean
 * Created by pujin on 2017/1/11.
 */
public class MsgDefTypeAdapter implements JsonSerializer<MsgDefinBean>, JsonDeserializer<MsgDefinBean> {

    private JsonObject object;

    @Override
    public MsgDefinBean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        object = json.getAsJsonObject();

        MsgDefinBean msgDefinBean = new MsgDefinBean();
        msgDefinBean.setType(parseInt("type"));
        msgDefinBean.setUser_name(parseString("user_name"));
        msgDefinBean.setSendtime(parseLong("sendtime"));
        msgDefinBean.setMessage_id(parseString("message_id"));
        msgDefinBean.setPublicKey(parseString("publicKey"));
        msgDefinBean.setUser_id(parseString("user_id"));
        msgDefinBean.setExt(parseString("ext"));
        msgDefinBean.setContent(parseString("content"));
        msgDefinBean.setUrl(parseString("url"));
        msgDefinBean.setExt1(parseString("ext1"));
        msgDefinBean.setLocationExt((GeoAddressBean) parseObjToObject("locationExt", GeoAddressBean.class));
        msgDefinBean.setSize(parseInt("size"));
        msgDefinBean.setImageOriginWidth(parseFloat("imageOriginWidth"));
        msgDefinBean.setImageOriginHeight(parseFloat("imageOriginHeight"));
        msgDefinBean.setSenderInfoExt((MsgSender) parseObjToObject("senderInfoExt", MsgSender.class));
        return msgDefinBean;
    }

    public String parseString(String key) {
        JsonElement element = object.get(key);
        if (element == null) return "";
        if (element.isJsonObject()) {
            return new Gson().toJson(element.getAsJsonObject());
        } else if (element.isJsonArray()) {
            return new Gson().toJson(element.getAsJsonArray());
        }
        return element.getAsString();
    }

    public int parseInt(String key) {
        JsonElement element = object.get(key);
        if (element == null) return 0;
        return element.getAsInt();
    }

    public long parseLong(String key) {
        JsonElement element = object.get(key);
        if (element == null) return 0;
        return element.getAsLong();
    }

    public float parseFloat(String key) {
        JsonElement element = object.get(key);
        if (element == null) return 0;
        return element.getAsFloat();
    }

    public String parseObjToString(String key) {
        JsonElement element = object.get(key);
        if (element == null) {
            return null;
        }
        return String.valueOf(object.get(key));
    }

    public Object parseObjToObject(String key, Class cls) {
        JsonElement element = object.get(key);
        if (element == null) {
            return null;
        }
        return new Gson().fromJson(String.valueOf(object.get(key)), cls);
    }

    @Override
    public JsonElement serialize(MsgDefinBean src, Type typeOfSrc, JsonSerializationContext context) {
        return null;
    }
}
