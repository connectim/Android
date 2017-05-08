package connect.ui.activity.wallet.bean;

/**
 * Created by Administrator on 2016/12/18.
 */
public class TranAddressBean {

    String address;
    long amount;

    public TranAddressBean(String address, long amount) {
        this.address = address;
        this.amount = amount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }
}
