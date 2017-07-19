package connect.wallet.cwallet.bean;

/**
 * Created by Administrator on 2017/7/18.
 */

public enum CurrencyEnum {
    BTC(0);

    private int code;

    CurrencyEnum(int code) {
        this.code = code;
    }

    public int getCode(){
        return code;
    }
}
