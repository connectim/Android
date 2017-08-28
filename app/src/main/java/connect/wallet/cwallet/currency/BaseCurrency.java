package connect.wallet.cwallet.currency;

import java.util.ArrayList;

import connect.database.green.bean.CurrencyEntity;
import connect.wallet.cwallet.inter.WalletListener;

/**
 * The currency base class defines the method of the currency base
 */

public abstract class BaseCurrency {
    // private key
    public static final int CATEGORY_PRIKEY = 1;
    // baseSeed
    public static final int CATEGORY_BASESEED = 2;
    // salt+seed
    public static final int CATEGORY_SALT_SEED = 3;

    /**
     * Signature transaction
     */
    public void signRawTx(String signraw) {}

    /**
     * Get currency information
     */
    public abstract void requestCoinInfo(WalletListener listener);

    /**
     * fee
     */
    public abstract void fee();

    /**
     * Get currency information
     */
    public abstract CurrencyEntity getCurrencyData();

    /**
     * Set currency information
     */
    public abstract void setCurrencyInfo(String payload, Integer status, WalletListener listener);

    /**
     * Get the currency address
     */
    public abstract String createAddress(String currencySeed);

    /**
     * Get the currency private key
     */
    public abstract String createPriKey(String baseSeed, String salt,int index);

    /**
     * Signature transaction
     */
    public abstract String getSignRawTrans(ArrayList<String> priList, String tvs, String rowhex);

    /**
     * The currency is converted to the corresponding Long type
     */
    public abstract long doubleToLongCurrency(double value);

    /**
     * Long converted to the corresponding currency
     */
    public abstract String longToDoubleCurrency(long value);

}

