package connect.utils.cryption;

import connect.database.MemoryDataManager;
import connect.utils.ConfigUtil;
import connect.wallet.jni.AllNativeMethod;
import com.google.protobuf.InvalidProtocolBufferException;

import protos.Connect;

/**
 * Decryption related methods
 */
public class DecryptionUtil {


    public static final int CRYPTION_TALKKEY_VER = 1;

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
     * Decryption Gcmdata returned to StructData
     *
     * @param gcmData
     * @return StructData
     */
    public static Connect.StructData decodeAESGCMStructData(Connect.GcmData gcmData) {
        return decodeAESGCMStructData(SupportKeyUril.EcdhExts.SALT, MemoryDataManager.getInstance().getPriKey(), gcmData);
    }

    public static Connect.StructData decodeAESGCMStructData(SupportKeyUril.EcdhExts exts, String priKey, Connect.GcmData gcmData) {
        return decodeAESGCMStructData(exts, priKey, ConfigUtil.getInstance().serverPubkey(), gcmData);
    }

    public static Connect.StructData decodeAESGCMStructData(SupportKeyUril.EcdhExts exts, String priKey, String pukkey, Connect.GcmData gcmData) {
        byte[] rss = SupportKeyUril.rawECDHkey(priKey, pukkey);
        return decodeAESGCMStructData(exts, rss, gcmData);
    }

    public static Connect.StructData decodeAESGCMStructData(SupportKeyUril.EcdhExts exts, byte[] ecdhKey, Connect.GcmData gcmData) {
        try {
            byte[] structs = DecryptionUtil.decodeAESGCM(exts, ecdhKey, gcmData);
            Connect.StructData structData = Connect.StructData.parseFrom(structs);
            return structData;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Decryption Gcmdata returns a byteArray containing
     *
     * @param exts
     * @param priKey
     * @param pukkey
     * @param gcmData
     * @return
     */
    public static byte[] decodeAESGCM(SupportKeyUril.EcdhExts exts, String priKey, String pukkey, Connect.GcmData gcmData) {
        byte[] rss = SupportKeyUril.rawECDHkey(priKey, pukkey);
        return DecryptionUtil.decodeAESGCM(exts, rss, gcmData);
    }

    public static synchronized byte[] decodeAESGCM(SupportKeyUril.EcdhExts exts, byte[] ecdhKey, Connect.GcmData gcmData) {
        //ecdhkey extension
        ecdhKey = SupportKeyUril.ecdhKeyExtends(exts, ecdhKey);

        byte[] iv = gcmData.getIv().toByteArray();
        byte[] add = "ConnectEncrypted".getBytes();
        byte[] cipher = gcmData.getCiphertext().toByteArray();
        byte[] tag = gcmData.getTag().toByteArray();

        byte[] rss = ecdhKey;
        byte[] rawData = AllNativeMethod.cdxtalkDecodeAESGCM(cipher, cipher.length,
                add, add.length, rss, rss.length, iv, iv.length, tag, tag.length);
        return rawData;
    }
}