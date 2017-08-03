package connect.utils.cryption;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ParamManager;
import connect.im.bean.Session;
import connect.activity.base.BaseApplication;
import connect.utils.StringUtil;
import connect.wallet.cwallet.currency.BaseCurrency;
import connect.wallet.jni.AllNativeMethod;
import protos.Connect;

/**
 * Random number generation key class Key...
 * Created by john on 2016/11/28.
 */

public class SupportKeyUril {

    private static String Tag = "SupportKeyUril";
    /** Key number expansion */
    public static final int CRYPTION_N = 17;
    /** Pin Version */
    public static final int PIN_VERSION = 1;
    /** hmac transform default salt */
    public static String HmacSalt = "49f41477fa1bfc3b4792d5233b6a659f4b";
    public static final int CRYPTION_TALKKEY_VER = 1;

    /**
     * Private key to public key
     *
     * @return
     */
    public static String getPubKeyFromPriKey(String priKey) {
        return AllNativeMethod.cdGetPubKeyFromPrivKey(priKey);
    }

    public static String getAddressFromPubkey(String pubkey) {
        return AllNativeMethod.cdGetBTCAddrFromPubKey(pubkey);
    }

    /**
     * 16-32 binary random number
     * modify Random
     *
     * @return
     */
    public static byte[] createrBinaryRandom() {
        Random random = new Random();
        int n = random.nextInt(17) + 16;
        byte[] byteRandom = SecureRandom.getSeed(n);
        return byteRandom;
    }

    /**
     * def XOR string
     */
    public static String xor(String strHex1,String strHex2){
        if(strHex1.length() != strHex2.length()){
            return "";
        }
        byte[] byte1 = StringUtil.hexStringToBytes(strHex1);
        byte[] byte2 = StringUtil.hexStringToBytes(strHex2);
        byte[] valueByte = SupportKeyUril.xor(byte1, byte2);
        return StringUtil.bytesToHexString(valueByte);
    }

    public static byte[] xor(byte[] byte1, byte[] byte2) {
        if(byte1.length != byte2.length){
            return null;
        }
        byte[] index = new byte[byte1.length];
        for (int i = 0; i < byte1.length; i++) {
            index[i] = (byte) (byte1[i] ^ byte2[i]);
        }
        return index;
    }

    /**
     * hmacSha512 encrypt
     *
     * @param data
     * @param key
     * @return
     */
    public static String hmacSHA512(String data, String key) {
        String result = "";
        if(TextUtils.isEmpty(data) || TextUtils.isEmpty(key)){
            return result;
        }
        byte[] bytesKey = key.getBytes();
        final SecretKeySpec secretKey = new SecretKeySpec(bytesKey, "HmacSHA512");
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(secretKey);
            final byte[] macData = mac.doFinal(data.getBytes());
            result = StringUtil.bytesToHexString(macData);
            //result = new String(, "ISO-8859-1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * def whether the private key
     */
    public static boolean checkPrikey(String prikey) {
        if(TextUtils.isEmpty(prikey)){
            return false;
        }
        return AllNativeMethod.checkPrivateKeyJ(prikey) > -1;
    }

    /**
     * def  whether the bitcoin address
     */
    public static boolean checkAddress(String address) {
        if(TextUtils.isEmpty(address)){
            return false;
        }
        return AllNativeMethod.checkAddressJ(address) > -1;
    }

    public static String localHashKey() {
        String key = AllNativeMethod.cdGetHash256(MemoryDataManager.getInstance().getPriKey());
        key = AllNativeMethod.cdGetHash256(key);
        return key;
    }

    /**
     * Generate ECDH cooperative key
     *
     * @param priKey
     * @param pubKey
     * @return
     */
    public static byte[] rawECDHkey(String priKey, String pubKey) {
        if (TextUtils.isEmpty(priKey)) {
            BaseApplication.getInstance().finishActivity();
        }
        return AllNativeMethod.cdxtalkgetRawECDHkey(priKey, pubKey);
    }

    public static byte[] byteSHA256(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.update(bytes);
        sha256.update(sha256.digest());
        return sha256.digest();
    }

    public static byte[] byteSHA512(byte[] bytes){
        MessageDigest sha256;
        try {
            sha256 = MessageDigest.getInstance("SHA-512");
            sha256.update(bytes);
            sha256.update(sha256.digest());
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return sha256.digest();
    }

    /**
     * Generate signature
     *
     * @param priKey
     * @param hash
     * @return
     */
    public static String signHash(String priKey, byte[] hash) {
        String signhash = null;
        try {
            signhash = AllNativeMethod.cdSignHash(priKey, StringUtil.bytesToHexString(byteSHA256(hash)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signhash;
    }

    /**
     * Verify the signature
     *
     * @param sign
     * @param data ConnectIm.GcmData
     * @return
     */
    public static synchronized boolean verifySign(String sign, byte[] data) {
        String hash = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(data));
        return AllNativeMethod.cdVerifySign(Session.getInstance().getUserCookie("TEMPCOOKIE").getPubKey(), hash, sign) != 1;
    }

    public static synchronized boolean verifySign(String puk, String sign, byte[] data) {
        String hash = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(data));
        return AllNativeMethod.cdVerifySign(puk, sign, hash) != 1;
    }

    /**
     * Generate 16-bit random number
     *
     * @return
     */
    public static byte[] cdJNISeed() {
        return SecureRandom.getSeed(16);
    }

    /**
     * Password salt to generate encryption private key, gestures
     */
    public static String cdSaltPri() {
        byte[] send = SupportKeyUril.cdJNISeed();
        String salt = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(send));
        return salt;
    }

    /**
     * ecdhkey extension
     */
    public static synchronized byte[] ecdhKeyExtends(EcdhExts exts, byte[] ecdhkey) {
        byte[] salts = null;
        switch (exts) {
            case NONE:
                break;
            case EMPTY:
                salts = new byte[64];
                ecdhkey = AllNativeMethod.cdxtalkPBKDF2HMACSHA512(ecdhkey, ecdhkey.length, salts, salts.length, 12, 32);
                break;
            case SALT:
                String index = ParamManager.getInstance().getString(ParamManager.GENERATE_TOKEN_SALT);
                salts = StringUtil.hexStringToBytes(index);
                ecdhkey = AllNativeMethod.cdxtalkPBKDF2HMACSHA512(ecdhkey, ecdhkey.length, salts, salts.length, 12, 32);
                break;
            case OTHER:
                salts = exts.getBytes();
                ecdhkey = AllNativeMethod.cdxtalkPBKDF2HMACSHA512(ecdhkey, ecdhkey.length, salts, salts.length, 12, 32);
                break;
        }
        return ecdhkey;
    }

    /**
     * ecdh Extension type
     */
    public enum EcdhExts {
        NONE,//Don't need to extension
        EMPTY,//Empty salt extension
        SALT,//token salt
        OTHER;//other extension

        byte[] bytes;

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }
    }

    /**
     * Generate encrypted TalkKey (encrypted private key, n = 17 for key expansion)
     */
    public static String createTalkKey(String priKey, String address, String passWord) {
        String priKey_16 = AllNativeMethod.cdgetRawPrivateKey(priKey);
        String talkKey = AllNativeMethod.cdxTalkKeyEncrypt(address, priKey_16, passWord, CRYPTION_N, CRYPTION_TALKKEY_VER);
        return talkKey;
    }

    /**
     * Decryption TalkKey
     *
     * @param talkKey
     * @param pass
     * @return
     */
    public static String decodeTalkKey(String talkKey, String pass) {
        String addressandpriKey_16 = AllNativeMethod.cdxTalkKeyDecrypt(talkKey, pass, CRYPTION_TALKKEY_VER);
        try {
            String priKey_16 = addressandpriKey_16.split("@")[1];
            String prikey = AllNativeMethod.cdgetRawToPrivateKey(priKey_16);
            return prikey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Pay password encryption
     */
    public static EncoPinBean encoPinDefult(int category,String value, String pass){
        return encoPin(category,value,pass,CRYPTION_N);
    }

    public static EncoPinBean encoPin(int category, String value, String pass,int n){
        if(BaseCurrency.CATEGORY_PRIKEY == category){
            value = StringUtil.bytesToHexString(value.getBytes());
        }
        EncoPinBean encoPinBean = new EncoPinBean();
        String payload = AllNativeMethod.connectWalletKeyEncrypt(value,pass,n,PIN_VERSION);
        encoPinBean.setPayload(payload);
        encoPinBean.setVersion(PIN_VERSION);
        encoPinBean.setN(n);
        return encoPinBean;
    }

    /**
     * Pay decryption password
     */
    public static String decodePinDefult(int category, String value, String pass){
        return decodePin(category ,value, pass, PIN_VERSION);
    }

    public static String decodePin(int category, String value, String pass,int verPin){
        String seed = AllNativeMethod.connectWalletKeyDecrypt(value,pass,verPin);
        if(BaseCurrency.CATEGORY_PRIKEY == category){
            seed = new String(StringUtil.hexStringToBytes(seed));
        }
        if(seed.contains("error")){
            seed = "";
        }
        return seed;
    }

    /**
     * Password encryption private key, gestures
     */
    public static String encodePri(String value, String salt, String pass) {
        return encodePri(value,salt,pass,12);
    }

    public static String encodePri(String value, String salt, String pass,int n) {
        byte[] passByte = pass.getBytes();
        byte[] saltByte = salt.getBytes();
        byte[] pbkdf = AllNativeMethod.cdxtalkPBKDF2HMACSHA512(passByte, passByte.length, saltByte, saltByte.length, n, 32);
        Connect.GcmData gcmData = null;
        try {
            gcmData = EncryptionUtil.encodeAESGCM(EcdhExts.NONE, pbkdf, value.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encryStr = StringUtil.bytesToHexString(gcmData.toByteArray());
        return encryStr;
    }

    /**
     * Password to decrypt the private key, gestures
     */
    public static String decodePri(String encryStr, String salt, String pass) {
        return decodePri(encryStr,salt,pass,12);
    }

    public static String decodePri(String encryStr, String salt, String pass,int n) {
        try {
            byte[] passByte = pass.getBytes();
            byte[] saltByte = salt.getBytes();
            byte[] pbkdf = AllNativeMethod.cdxtalkPBKDF2HMACSHA512(passByte, passByte.length, saltByte, saltByte.length, n, 32);
            Connect.GcmData gcmData = Connect.GcmData.parseFrom(StringUtil.hexStringToBytes(encryStr));
            byte[] contant = DecryptionUtil.decodeAESGCM(SupportKeyUril.EcdhExts.NONE, pbkdf, gcmData);
            String priKey = "";
            try {
                priKey = new String(contant,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return priKey;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }

}
