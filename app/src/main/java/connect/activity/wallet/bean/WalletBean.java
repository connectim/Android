package connect.activity.wallet.bean;

/**
 * Created by Administrator on 2017/7/10 0010.
 */

public class WalletBean {
    private String payload;
    private int ver;
    private int version;
    private String checkSum;

    public WalletBean() {
    }

    public WalletBean(String payload ,int ver, int version, String checkSum) {
        this.payload = payload;
        this.ver = ver;
        this.version = version;
        this.checkSum = checkSum;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public int getVer() {
        return ver;
    }

    public void setVer(int ver) {
        this.ver = ver;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }
}
