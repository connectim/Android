package connect.ui.activity.wallet.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/12/20.
 */
public class SendOutBean implements Serializable{

    private String type;
    private int number;
    private String url;
    private long deadline;
    private String hashId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getDeadline() {
        return deadline;
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public String getHashId() {
        return hashId;
    }

    public void setHashId(String hashId) {
        this.hashId = hashId;
    }
}
