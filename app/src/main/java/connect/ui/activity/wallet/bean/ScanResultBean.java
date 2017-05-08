package connect.ui.activity.wallet.bean;

/**
 * Created by Administrator on 2017/2/26.
 */

public class ScanResultBean {

    private String address;
    private String tip;
    private String amount;
    private String token;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
