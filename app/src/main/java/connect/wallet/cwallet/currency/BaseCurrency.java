package connect.wallet.cwallet.currency;

import java.util.ArrayList;

import connect.database.green.bean.CurrencyEntity;
import connect.wallet.cwallet.inter.WalletListener;

/**
 * The currency base class defines the currency base method
 * Created by Administrator on 2017/7/18.
 */

public abstract class BaseCurrency {

    // 1:Pure private key，2:baseSeed，3:salt+seed
    public static final int CATEGORY_PRIKEY = 1;
    public static final int CATEGORY_BASESEED = 2;
    public static final int CATEGORY_SALT_SEED = 3;

    public void publish(String hashid, String rawtx) {

    }

    public void signRawTx(String signraw) {

    }


    public abstract void requestCoinInfo(WalletListener listener);


    public abstract void fee();


    public abstract CurrencyEntity getCurrencyData();


    public abstract void setCurrencyInfo(final CurrencyEntity currencyEntity, WalletListener listener);


    public abstract String createAddress(String currencySeed);


    public abstract String createPriKey(String baseSeed, String salt,int index);


    public abstract String getSignRawTrans(ArrayList<String> priList, String tvs, String rowhex);


    public abstract long doubleToLongCurrency(double value);


    public abstract String longToDoubleCurrency(long value);

}

