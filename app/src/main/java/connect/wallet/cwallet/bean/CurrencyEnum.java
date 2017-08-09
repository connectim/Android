package connect.wallet.cwallet.bean;

/**
 * type of currency
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

    public static CurrencyEnum getCurrency(int code){
        switch (code){
            case 0:
                return BTC;
            default:
                break;
        }
        return null;
    }
}
