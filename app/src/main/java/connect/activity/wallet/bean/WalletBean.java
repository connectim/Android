package connect.activity.wallet.bean;

/**
 * Created by Administrator on 2017/7/10 0010.
 */

public class WalletBean {
    private String payload;
    private String salt;
    private int n;
    private int version;
    private String wid;
    private int status;

    public WalletBean() {
    }

    public WalletBean(String payload, String salt, int n, int version, String wid, int status) {
        this.payload = payload;
        this.salt = salt;
        this.n = n;
        this.version = version;
        this.wid = wid;
        this.status = status;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getWid() {
        return wid;
    }

    public void setWid(String wid) {
        this.wid = wid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
