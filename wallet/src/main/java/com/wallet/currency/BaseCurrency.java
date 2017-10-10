package com.wallet.currency;

import com.wallet.bean.CurrencyBean;

import java.util.ArrayList;
import java.util.List;

/**
 * The monetary base management class
 */

public interface BaseCurrency {

    /** Pure private key version */
    int CATEGORY_PRIKEY = 1;
    /** The original seed version */
    int CATEGORY_BASESEED = 2;
    /** Currency seed version */
    int CATEGORY_CURRENCY = 3;

    /**
     * Create a currency
     *
     * @param type The original seed
     * @param baseSeed original seed  private key  currency
     * @return Currency information(baseSeed currencySeed salt index masterAddress)
     */
    CurrencyBean createCurrency(int type, String baseSeed);

    /**
     * Increase the money's address
     *
     * @param baseSeed The original seed
     * @param salt Generate monetary seeds of salt
     * @param index digits
     * @return Currency information(baseSeed currencySeed salt index masterAddress)
     */
    CurrencyBean addCurrencyAddress(String baseSeed, String salt, int index);

    /**
     * Access to the private key for the address
     *
     * @param baseSeed The original seed
     * @param salt Generate monetary seeds of salt
     * @param indexList To generate address digits array
     * @return Corresponding address private key array
     */
    List<String> getPriKeyFromAddressIndex(String baseSeed, String salt, List<Integer> indexList);

    /**
     * Signature trading
     *
     * @param priList The private key array
     * @param tvs The input string
     * @param rowHex The original trading
     * @return Signature trading
     */
    String getSignRawTrans(ArrayList<String> priList, String tvs, String rowHex);

    /**
     * Broadcasting deals
     *
     * @param rawString Signature trading
     * @return Tx to query transaction details
     */
    String broadcastTransfer(String rawString);



}
