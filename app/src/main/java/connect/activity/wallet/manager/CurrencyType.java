package connect.activity.wallet.manager;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/7 0007.
 */

public enum CurrencyType implements Serializable {
    BTC("btc",0);

    private final String name;
    private final int code;

    CurrencyType(String name, int code) {
        this.name = name;
        this.code = code;
    }

    public String getName(){
        return name;
    }

    public int getCode(){
        return code;
    }

}
