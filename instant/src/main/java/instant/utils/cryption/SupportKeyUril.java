package instant.utils.cryption;

import android.text.TextUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import connect.wallet.jni.AllNativeMethod;
import instant.utils.StringUtil;

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
     * 16-32 binary random number
     */
    public static byte[] createBinaryRandom() {
        Random random = new Random();
        int n = random.nextInt(17) + 16;
        byte[] byteRandom = SecureRandom.getSeed(n);
        return byteRandom;
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
}
