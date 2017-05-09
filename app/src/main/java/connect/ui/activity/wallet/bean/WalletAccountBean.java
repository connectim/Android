package connect.ui.activity.wallet.bean;

/**
 * Created by Administrator on 2017/1/8.
 */
public class WalletAccountBean {

    Long amount;
    Long avaAmount;

    public WalletAccountBean(Long amount, Long avaAmount) {
        this.amount = amount;
        this.avaAmount = avaAmount;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public Long getAvaAmount() {
        return avaAmount;
    }

    public void setAvaAmount(Long avaAmount) {
        this.avaAmount = avaAmount;
    }
}
