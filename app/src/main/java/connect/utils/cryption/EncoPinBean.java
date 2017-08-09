package connect.utils.cryption;

/**
 * Created by Administrator on 2017/7/13 0013.
 */

public class EncoPinBean {
    private String payload;
    private int version;
    private int n;

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }
}
