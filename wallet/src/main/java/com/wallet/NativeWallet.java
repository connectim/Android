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
     * 获取对应币种管理器
     *
     * @param currencyEnum 币种类型
     * @return 币种管理类实例
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
     * 创建币种
     */
    public void createCurrency(CurrencyEnum currencyEnum, int type, String value, WalletListener walletListener){
        BaseCurrency baseCurrency = initCurrency(currencyEnum);
        CurrencyBean currencyBean = baseCurrency.createCurrency(type, value);
        walletListener.success(currencyBean);
    }

    /**
     * 在对应币种增加address(一个币种可对应多个地址)
     */
    public void addCurrencyAddress(CurrencyEnum currencyEnum, String baseSeed, String salt, int index, WalletListener walletListener){
        BaseCurrency baseCurrency = initCurrency(currencyEnum);
        CurrencyBean currencyBean = baseCurrency.addCurrencyAddress(baseSeed, salt, index);
        walletListener.success(currencyBean);
    }

    /**
     * 获取对应地址的私钥
     */
    public void getPriKeyFromAddressIndex(CurrencyEnum currencyEnum, String baseSeed, String salt, List<Integer> indexList, WalletListener walletListener){
        BaseCurrency baseCurrency = initCurrency(currencyEnum);
        List<String> priKeyList = baseCurrency.getPriKeyFromAddressIndex(baseSeed, salt, indexList);
        walletListener.success(priKeyList);
    }

    /**
     * 签名交易
     */
    public void getSignRawTrans(CurrencyEnum currencyEnum, ArrayList<String> priList, String tvs, String rowHex, WalletListener walletListener){
        BaseCurrency baseCurrency = initCurrency(currencyEnum);
        String rawString = baseCurrency.getSignRawTrans(priList, tvs, rowHex);
        walletListener.success(rawString);
    }

    /**
     * 生成助记词
     */
    public String getWordsFromVale(String value){
        return baseWallet.getWordsFromVale(value);
    }

    /**
     * 解密助记词
     */
    public String getValueFromWords(String words){
        return baseWallet.getValueFromWords(words);
    }

    /**
     * 支付密码加密数据
     */
    public EncryptionPinBean encryptionPin(int category, String value, String pass){
        return baseWallet.encryptionPinDefault(category, value, pass);
    }

    /**
     * 支付密码解密数据
     */
    public String decryptionPin(int category, String value, String pass){
        return baseWallet.decryptionPinDefault(category, value, pass);
    }

}
