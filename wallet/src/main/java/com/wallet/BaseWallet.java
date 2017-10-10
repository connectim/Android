package com.wallet;

import com.wallet.bean.EncryptionPinBean;
import com.wallet.currency.BaseCurrency;
import com.wallet.utils.WalletUtil;

import connect.wallet.jni.AllNativeMethod;

/**
 * Basic purse function
 */

public class BaseWallet {

    /** Password encryption version */
    public static final int PIN_VERSION = 1;
    /** Encryption is difficult */
    public static final int ENCRYPTION_N = 17;

    /**
     * Generate the mnemonic word
     *
     * @param vale Need to generate a string
     * @return The mnemonic word
     */
    public String getWordsFromVale(String vale){
        String words = AllNativeMethod.cdGetBIP39WordsFromSeed(vale);
        return words;
    }

    /**
     * Decryption mnemonic word
     *
     * @param words The mnemonic word
     * @return Data
     */
    public String getValueFromWords(String words){
        String value = AllNativeMethod.cdGetSeedFromBIP39Words("", words);
        return value;
    }

    /**
     * Pay password encryption
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
     * Pay decryption password
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
