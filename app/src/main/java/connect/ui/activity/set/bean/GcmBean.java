package connect.ui.activity.set.bean;

/**
 * Created by Administrator on 2016/12/7.
 */
public class GcmBean {
    private String iv;
    private String aad;
    private String tag;
    private String ciphertext;

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getAdd() {
        return aad;
    }

    public void setAdd(String add) {
        this.aad = add;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public void setCiphertext(String ciphertext) {
        this.ciphertext = ciphertext;
    }
}
