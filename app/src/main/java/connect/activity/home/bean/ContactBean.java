package connect.activity.home.bean;

/**
 * Created by Administrator on 2017/5/25 0025.
 */

public class ContactBean {

    private String uid;
    private String name;
    private String avatar;
    /**
     * 1.friend request 2.group 3.Frequent contacts 4.friend 5.Number of friends 6.Connect system
     */
    private int status;
    private String tips;
    private int count;
    private String ou;

    public ContactBean() {
    }

    public ContactBean(int status, String name) {
        this.status = status;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOu() {
        return ou;
    }

    public void setOu(String ou) {
        this.ou = ou;
    }
}
