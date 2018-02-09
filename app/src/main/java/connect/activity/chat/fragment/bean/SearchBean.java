package connect.activity.chat.fragment.bean;

/**
 * Created by Administrator on 2018/1/31 0031.
 */

public class SearchBean {

    private String uid;
    private String avatar;
    private String name;
    private String searchStr;
    private String hinit;
    private int style;
    private int status;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHinit() {
        return hinit;
    }

    public void setHinit(String hinit) {
        this.hinit = hinit;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public String getSearchStr() {
        return searchStr;
    }

    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
