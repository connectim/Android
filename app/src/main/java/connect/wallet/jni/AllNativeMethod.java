package connect.wallet.jni;


import java.io.Serializable;

public class AllNativeMethod implements Serializable{

    static {
        System.loadLibrary("add");
    }

    public static native void testmode();

    public static native String cdCreateNewPrivKey();

    public static native String cdGetPubKeyFromPrivKey(String privatekey);

    public static native String cdGetBTCAddrFromPubKey(String pubkey);

    public static native String cdGetScriptPubKeyFromPubKey(String pubkey);

    public static native String cdCreateMultisigAddress(String jsonMultiSign);

    public static native String cdCreateSeed(int randomNum, int version);

    public static native String cdGetBIP39WordsFromSeed(String seed);

    public static native String cdGetSeedFromBIP39Words(String password, String words);

    public static native String cdGetPrivKeyFromSeedBIP44(String seed, int purpose, int coin, int account, int isInternal, int addrIndex);

    public static native String cdGetPubKeyFromSeedBIP44(String seed, int purpose, int coin, int account, int isInternal, int addrIndex);

    public static native String cdGetAccountMasterPubKeyFromSeedBIP44(String seed, int purpose, int coin, int account);

    public static native String cdGetPubKeyFromAccountMasterPubKeyBIP44(String masterPubKey, int isInternal, int addrIndex);

    public static native String cdCreateRawTranscation(String rawTranscation);

    public static native String cdSignRawTranscation(String signTranscation);

    public static native String cdSignHash(String privatekey, String hashHexStr);

    public synchronized static native int cdVerifySign(String publickey, String hashHexStr, String signData);

    public static native String cdSignMessage(String signMessage);

    public static native int cdVerifyMessage(String verifyMessage);

    public static native String cdEncriptWithMD5(String password, String seed);

    public static native String cdDecriptAndCheckMD5(String password, String seed);

    public static native String cdECCencryptEx(String privatekey, String publickey, String data);

    public static native String cdECCdecryptEx(String privatekey, String publickey, String data);

    public static native String cdSimplePbkdf2HmacSha512(String content, String salt);

    public synchronized static native String cdGetHash256(String data);

    //Encrypted string generated
    public static native String cdxTalkKeyEncrypt(String userID, String key, String pwd, int n, int ver);

    public static native String cdxTalkKeyDecrypt(String enString, String pwd, int ver);

    public static native String cdgetRawPrivateKey(String privateKey);

    public static native String cdgetRawToPrivateKey(String rawPrivateKey);
    public static native int checkAddressJ(String address);
    public static native int checkPrivateKeyJ(String privatekey);

    public static native String connectWalletKeyEncrypt(String seed, String pwd, int n, int version);

    public static native String connectWalletKeyDecrypt(String enString, String pwd, int n);

    /**
     * The key in password with salt
     * <p/>
     * Using the method of the strings are used getBytes () to obtain the byte array, length: the corresponding byte data length
     *
     * @param pwd
     * @param pwdLen
     * @param salt
     * @param saltLen
     * @param n
     * @return
     */
    public static native byte[] cdxtalkPBKDF2HMACSHA512(byte[] pwd, int pwdLen, byte[] salt, int saltLen, int n);

    public static native byte[] cdxtalkPBKDF2HMACSHA512(byte[] pwd,int pwdLen,byte[] salt,int saltLen,int n,int keyLen);

    /**
     * Encryption function
     *
     * @param key
     * @param keyLen
     * @param content
     * @param contentLen
     * @return
     */
    public static native byte[] cdxtalkEncodeAES(byte[] key, int keyLen, byte[] content, int contentLen);

    /**
     * Decryption function
     *
     * @param key
     * @param keyLen
     * @param encoded
     * @param encodedLen
     * @return
     */
    public static native byte[] cdxtalkDecodeAES(byte[] key, int keyLen, byte[] encoded, int encodedLen);

    public static native byte[] cdxtalkgetECDHkey(String privatekey, String publickey);

    /**
     * def load aeskey
     * param
     * return
     * 2016/5/23 18:01
     */
    public static native byte[] cdxtalkgetRawECDHkey(String privatekey, String publickey);

    public static native byte[] cdxtalkECDHencrypt(byte[] ECDHkey, int ecdhLen, byte[] indata, int indataLen);

    public static native byte[] cdxtalkECDHdecrypt(byte[] ECDHkey, int ecdhLen, byte[] indata, int indataLen);


    public static native byte[] cdxtalkEncodeAESCBCIV(byte[] iv, int ivLen, byte[] key, int keyLen, byte[] content, int contentLen);

    public static native byte[] cdxtalkDecodeAESCBCIV(byte[] iv, int ivLen, byte[] key, int keyLen, byte[] content, int contentLen);


    public static native GCMModel cdxtalkEncodeAESGCM(
            byte[] indata, int indataLen,
            byte[] aad, int aadLen,
            byte[] key, int keyLen,
            byte[] iv, int ivLen
    );

    public static native byte[] cdxtalkDecodeAESGCM(
            byte[] endata, int endataLen,
            byte[] aad, int aadLen,
            byte[] key, int keyLen,
            byte[] iv, int ivLen,
            byte[] tag, int tagLen
    );
}
