package com.wallet;

import com.wallet.bean.CurrencyBean;
import com.wallet.bean.CurrencyEnum;
import com.wallet.bean.EncryptionPinBean;
import com.wallet.currency.BaseCurrency;
import com.wallet.currency.BtcCurrency;
import com.wallet.inter.WalletListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 钱包对外暴露调用类
 */

public class NativeWallet {

    private static NativeWallet mNativeWallet;
    private BaseWallet baseWallet;
    private Map<CurrencyEnum, BaseCurrency> baseCurrencyMap;

    public static NativeWallet getInstance() {
        if (mNativeWallet == null) {
            synchronized (NativeWallet.class) {
                if (mNativeWallet == null) {
                    mNativeWallet = new NativeWallet();
                }
            }
        }
        return mNativeWallet;
    }

    public NativeWallet() {
        baseCurrencyMap = new HashMap<>();
        baseWallet = new BaseWallet();
    }

    /**
     * To obtain corresponding currency manager
     *
     * @param currencyEnum Currency type
     * @return Currency management class instance
     */
    public BaseCurrency initCurrency(CurrencyEnum currencyEnum) {
        if (baseCurrencyMap == null) {
            baseCurrencyMap = new HashMap<>();
        }
        BaseCurrency baseCurrency = baseCurrencyMap.get(currencyEnum);
        if (baseCurrency == null) {
            switch (currencyEnum) {
                case BTC:
                    baseCurrency = new BtcCurrency();
                    baseCurrencyMap.put(currencyEnum, baseCurrency);
                    break;
                default:
                    break;
            }
        }
        return baseCurrency;
    }

    /**
     * Create a currency
     */
    public void createCurrency(CurrencyEnum currencyEnum, int type, String value, WalletListener walletListener){
        BaseCurrency baseCurrency = initCurrency(currencyEnum);
        CurrencyBean currencyBean = baseCurrency.createCurrency(type, value);
        walletListener.success(currencyBean);
    }

    /**
     * In the corresponding currency increase address,a currency can correspond to multiple addresses
     */
    public void addCurrencyAddress(CurrencyEnum currencyEnum, String baseSeed, String salt, int index, WalletListener walletListener){
        BaseCurrency baseCurrency = initCurrency(currencyEnum);
        CurrencyBean currencyBean = baseCurrency.addCurrencyAddress(baseSeed, salt, index);
        walletListener.success(currencyBean);
    }

    /**
     * Access to the private key for the address
     */
    public void getPriKeyFromAddressIndex(CurrencyEnum currencyEnum, String baseSeed, String salt, List<Integer> indexList, WalletListener walletListener){
        BaseCurrency baseCurrency = initCurrency(currencyEnum);
        List<String> priKeyList = baseCurrency.getPriKeyFromAddressIndex(baseSeed, salt, indexList);
        walletListener.success(priKeyList);
    }

    /**
     * Signature trading
     */
    public void getSignRawTrans(CurrencyEnum currencyEnum, ArrayList<String> priList, String tvs, String rowHex, WalletListener walletListener){
        BaseCurrency baseCurrency = initCurrency(currencyEnum);
        String rawString = baseCurrency.getSignRawTrans(priList, tvs, rowHex);
        walletListener.success(rawString);
    }

    /**
     * Generate the mnemonic word
     */
    public String getWordsFromVale(String value){
        return baseWallet.getWordsFromVale(value);
    }

    /**
     * Decryption mnemonic word
     */
    public String getValueFromWords(String words){
        return baseWallet.getValueFromWords(words);
    }

    /**
     * Pay the password to encrypt data
     */
    public EncryptionPinBean encryptionPin(int category, String value, String pass){
        return baseWallet.encryptionPinDefault(category, value, pass);
    }

    /**
     * Pay the password to decrypt the data
     */
    public String decryptionPin(int category, String value, String pass){
        return baseWallet.decryptionPinDefault(category, value, pass);
    }

}
