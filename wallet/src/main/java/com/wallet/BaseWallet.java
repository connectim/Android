package com.wallet;

import com.wallet.bean.EncryptionPinBean;
import com.wallet.currency.BaseCurrency;
import com.wallet.utils.WalletUtil;

import connect.wallet.jni.AllNativeMethod;

/**
 * 基础钱包功能
 */

public class BaseWallet {

    /** 密码加密版本 */
    public static final int PIN_VERSION = 1;
    /** 加密难度 */
    public static final int ENCRYPTION_N = 17;

    /**
     * 生成助记词
     *
     * @param vale 需要生成的字符串
     * @return 助记词
     */
    public String getWordsFromVale(String vale){
        String words = AllNativeMethod.cdGetBIP39WordsFromSeed(vale);
        return words;
    }

    /**
     * 解密助记词
     *
     * @param words 助记词
     * @return 助记词
     */
    public String getValueFromWords(String words){
        String value = AllNativeMethod.cdGetSeedFromBIP39Words("", words);
        return value;
    }

    /**
     * 加密支付密码
     */
    public EncryptionPinBean encryptionPinDefault(int category, String value, String pass){
        return encryptionPin(category,value,pass,ENCRYPTION_N);
    }

    public EncryptionPinBean encryptionPin(int category, String value, String pass, int n){
        if(BaseCurrency.CATEGORY_PRIKEY == category){
            value = WalletUtil.bytesToHexString(value.getBytes());
        }
        EncryptionPinBean encryptionPinBean = new EncryptionPinBean();
        String payload = AllNativeMethod.connectWalletKeyEncrypt(value,pass,n,PIN_VERSION);
        encryptionPinBean.setPayload(payload);
        encryptionPinBean.setVersion(PIN_VERSION);
        encryptionPinBean.setN(n);
        return encryptionPinBean;
    }

    /**
     * 解密支付密码
     */
    public String decryptionPinDefault(int category, String value, String pass){
        return decryptionPin(category ,value, pass, PIN_VERSION);
    }

    public String decryptionPin(int category, String value, String pass,int verPin){
        String seed = AllNativeMethod.connectWalletKeyDecrypt(value,pass,verPin);
        if(seed.contains("error")){
            return  "";
        }
        if(BaseCurrency.CATEGORY_PRIKEY == category){
            seed = new String(WalletUtil.hexStringToBytes(seed));
        }
        return seed;
    }

}
