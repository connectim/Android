package connect.activity.chat.bean;

import android.text.TextUtils;

import connect.database.MemoryDataManager;
import connect.ui.activity.R;
import connect.activity.base.BaseApplication;

import java.io.Serializable;

/**
 * content bean
 * Created by gtq on 2016/11/23.
 */
public class MsgDefinBean implements Serializable{
    private int type;
    private String user_name;
    private long sendtime;
    private String message_id;
    private String publicKey;
    private String user_id;
    private String ext;
    private String content;
    private String url;
    private String ext1;
    private GeoAddressBean locationExt;
    private int size;
    private float imageOriginWidth;
    private float imageOriginHeight;

    private MsgSender senderInfoExt;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public long getSendtime() {
        return sendtime;
    }

    public void setSendtime(long sendtime) {
        this.sendtime = sendtime;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getExt1() {
        return ext1;
    }

    public void setExt1(String ext1) {
        this.ext1 = ext1;
    }

    public GeoAddressBean getLocationExt() {
        return locationExt;
    }

    public void setLocationExt(GeoAddressBean locationExt) {
        this.locationExt = locationExt;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public float getImageOriginWidth() {
        return imageOriginWidth;
    }

    public void setImageOriginWidth(float imageOriginWidth) {
        this.imageOriginWidth = imageOriginWidth;
    }

    public float getImageOriginHeight() {
        return imageOriginHeight;
    }

    public void setImageOriginHeight(float imageOriginHeight) {
        this.imageOriginHeight = imageOriginHeight;
    }

    public MsgSender getSenderInfoExt() {
        return senderInfoExt;
    }

    public void setSenderInfoExt(MsgSender senderInfoExt) {
        this.senderInfoExt = senderInfoExt;
    }

    public MsgDirect msgDirect() {
        MsgDirect direct = null;
        MsgSender sender = getSenderInfoExt();
        if (sender == null) {
            direct = (getPublicKey() == null || MemoryDataManager.getInstance().getPubKey().equals(getPublicKey())) ? MsgDirect.From : MsgDirect.To;
        } else {
            if (TextUtils.isEmpty(sender.publickey)) {
                direct = MemoryDataManager.getInstance().getAddress().equals(sender.address) ? MsgDirect.To : MsgDirect.From;
            } else {
                direct = MemoryDataManager.getInstance().getPubKey().equals(sender.publickey) ? MsgDirect.To : MsgDirect.From;
            }
        }
        return direct;
    }
}
