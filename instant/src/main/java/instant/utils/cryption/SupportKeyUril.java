package instant.utils.cryption;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import connect.wallet.jni.AllNativeMethod;
import instant.utils.StringUtil;
import protos.Connect;

/**
 * Random number generation key class Key.
 */

public class SupportKeyUril {

    /** Key number expansion */
    public static final int ENCRYPTION_N = 17;
    /** Pin Version */
    public static final int PIN_VERSION = 1;
    /** HMAC transform default salt */
    public static String SaltHMAC = "49f41477fa1bfc3b4792d5233b6a659f4b";
    public static final int ENCRYPTION_TALK_VERSION = 1;

    /**
     * Random private key
     */
    public static String getNewPriKey() {
        return AllNativeMethod.cdCreateNewPrivKey();
    }

    /**
     * Private key to public key
     */
    public static String getPubKeyFromPriKey(String priKey) {
        return AllNativeMethod.cdGetPubKeyFromPrivKey(priKey);
    }

    /**
     * Public key to address
     */
    public static String getAddressFromPubKey(String pubKey) {
        return AllNativeMethod.cdGetBTCAddrFromPubKey(pubKey);
    }

    /**
     * Password salt to generate encryption private key, gestures
     */
    public static String getSaltPri() {
        byte[] send = SecureRandom.getSeed(16);
        String salt = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(send));
        return salt;
    }

    /**
     * 16-32 binary random number
     */
    public static byte[] createBinaryRandom() {
        Random random = new Random();
        int n = random.nextInt(17) + 16;
        byte[] byteRandom = SecureRandom.getSeed(n);
        return byteRandom;
    }

    public static byte[] createSecureRandom(int num) {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[num];
        random.nextBytes(bytes);
        return bytes;
    }

    public static byte[] sha512(byte[] content) {
        MessageDigest md;
        byte[] shaBytes = null;
        try {
            md = MessageDigest.getInstance("SHA-512");
            shaBytes = md.digest(content);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return shaBytes;
    }

    /**
     * def whether the private key
     */
    public static boolean checkPriKey(String priKey) {
        if(TextUtils.isEmpty(priKey)){
            return false;
        }
        return AllNativeMethod.checkPrivateKeyJ(priKey) > -1;
    }

    /**
     * def  whether the bitCoin address
     */
    public static boolean checkAddress(String address) {
        if(TextUtils.isEmpty(address)){
            return false;
        }
        return AllNativeMethod.checkAddressJ(address) > -1;
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

    public static String localHashKey() {
        String key = AllNativeMethod.cdGetHash256(SupportKey.getInstance().getPrivateKey());
        key = AllNativeMethod.cdGetHash256(key);
        return key;
    }

    /**
     * Generate ECDH cooperative key
     */
    public static byte[] getRawECDHKey(String priKey, String pubKey) {
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
     */
    public static String signHash(String priKey, byte[] hash) {
        String signHash = null;
        try {
            signHash = AllNativeMethod.cdSignHash(priKey, StringUtil.bytesToHexString(byteSHA256(hash)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return signHash;
    }

    public static synchronized boolean verifySign(String puk, String sign, byte[] data) {
        String hash = AllNativeMethod.cdGetHash256(StringUtil.bytesToHexString(data));
        return AllNativeMethod.cdVerifySign(puk, sign, hash) != 1;
    }

    /**
     * Generate encrypted TalkKey (encrypted private key, n = 17 for key expansion)
     */
    public static String createTalkKey(String priKey, String address, String passWord) {
        // To obtain the original private key
        String priKeyRaw = AllNativeMethod.cdgetRawPrivateKey(priKey);
        String talkKey = AllNativeMethod.cdxTalkKeyEncrypt(address, priKeyRaw, passWord, ENCRYPTION_N, ENCRYPTION_TALK_VERSION);
        return talkKey;
    }

    /**
     * Decryption TalkKey
     */
    public static String decodeTalkKey(String talkKey, String pass) {
        String talkKeyDecryption = AllNativeMethod.cdxTalkKeyDecrypt(talkKey, pass, ENCRYPTION_TALK_VERSION);
        try {
            String priKeyRaw = talkKeyDecryption.split("@")[1];
            // The original private key converted to compress the private key
            String priKey = AllNativeMethod.cdgetRawToPrivateKey(priKeyRaw);
            return priKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Pay password encryption
     */
    public static EncryptionPinBean encryptionPinDefault(int category, String value, String pass){
        return encryptionPin(category,value,pass,ENCRYPTION_N);
    }

    public static EncryptionPinBean encryptionPin(int category, String value, String pass, int n){
        if(1 == category){
            value = StringUtil.bytesToHexString(value.getBytes());
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
    public static String decryptionPinDefault(int category, String value, String pass){
        return decryptionPin(category ,value, pass, PIN_VERSION);
    }

    public static String decryptionPin(int category, String value, String pass,int verPin){
        String seed = AllNativeMethod.connectWalletKeyDecrypt(value,pass,verPin);
        if(seed.contains("error")){
            return  "";
        }
        if(1 == category){
            seed = new String(StringUtil.hexStringToBytes(seed));
        }
        return seed;
    }

    /**
     * Password encryption private key, gestures
     */
    public static String encryptionPri(String value, String salt, String pass) {
        return encryptionPri(value,salt,pass,12);
    }

    public static String encryptionPri(String value, String salt, String pass,int n) {
        byte[] passByte = pass.getBytes();
        byte[] saltByte = salt.getBytes();
        byte[] talkPBKDF = AllNativeMethod.cdxtalkPBKDF2HMACSHA512(passByte, passByte.length, saltByte, saltByte.length, n, 32);
        Connect.GcmData gcmData = null;
        try {
            gcmData = EncryptionUtil.encodeAESGCM(EncryptionUtil.ExtendedECDH.NONE, talkPBKDF, value.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String encryptionStr = StringUtil.bytesToHexString(gcmData.toByteArray());
        return encryptionStr;
    }

    /**
     * Password to decrypt the private key, gestures
     */
    public static String decryptionPri(String encryptionStr, String salt, String pass) {
        return decryptionPri(encryptionStr,salt,pass,12);
    }

    public static String decryptionPri(String encryptionStr, String salt, String pass,int n) {
        try {
            byte[] passByte = pass.getBytes();
            byte[] saltByte = salt.getBytes();
            byte[] talkPBKDF = AllNativeMethod.cdxtalkPBKDF2HMACSHA512(passByte, passByte.length, saltByte, saltByte.length, n, 32);
            Connect.GcmData gcmData = Connect.GcmData.parseFrom(StringUtil.hexStringToBytes(encryptionStr));
            byte[] content = DecryptionUtil.decodeAESGCM(EncryptionUtil.ExtendedECDH.NONE, talkPBKDF, gcmData);
            String priKey = "";
            try {
                priKey = new String(content,"UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return priKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
