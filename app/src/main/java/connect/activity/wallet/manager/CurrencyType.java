package connect.activity.wallet.manager;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/7/7 0007.
 */

public enum CurrencyType implements Serializable {
    BTC("btc");

    private final String name;

    CurrencyType(String name) {
        this.name = name;
    }
}
