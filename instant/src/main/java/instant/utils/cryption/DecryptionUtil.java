package instant.utils.cryption;

import com.google.protobuf.InvalidProtocolBufferException;

import connect.wallet.jni.AllNativeMethod;
import protos.Connect;

/**
 * Decryption related methods
 */
public class DecryptionUtil {

    /**
     * Decryption gcmData returns a byteArray containing
     */
    public static byte[] decodeAESGCM(EncryptionUtil.ExtendedECDH extendedECDH, String priKey, String pubKey, Connect.GcmData gcmData) {
        byte[] rawECDHkey = SupportKeyUril.getRawECDHKey(priKey, pubKey);
        return DecryptionUtil.decodeAESGCM(extendedECDH, rawECDHkey, gcmData);
    }

    public static Connect.StructData decodeAESGCMStructData(EncryptionUtil.ExtendedECDH extendedECDH, String priKey, Connect.GcmData gcmData) {
        return decodeAESGCMStructData(extendedECDH, priKey, "", gcmData);
    }

    public static Connect.StructData decodeAESGCMStructData(EncryptionUtil.ExtendedECDH extendedECDH, String priKey, String pubKey, Connect.GcmData gcmData) {
        byte[] rss = SupportKeyUril.getRawECDHKey(priKey, pubKey);
        return decodeAESGCMStructData(extendedECDH, rss, gcmData);
    }

    public static Connect.StructData decodeAESGCMStructData(EncryptionUtil.ExtendedECDH extendedECDH, byte[] byteECDH, Connect.GcmData gcmData) {
        try {
            byte[] dataAESGCM = DecryptionUtil.decodeAESGCM(extendedECDH, byteECDH, gcmData);
            Connect.StructData structData = Connect.StructData.parseFrom(dataAESGCM);
            return structData;
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static synchronized byte[] decodeAESGCM(EncryptionUtil.ExtendedECDH extendedECDH, byte[] rawECDHkey, Connect.GcmData gcmData) {
        rawECDHkey = EncryptionUtil.getKeyExtendedECDH(extendedECDH, rawECDHkey);

        byte[] iv = gcmData.getIv().toByteArray();
        byte[] add = "ConnectEncrypted".getBytes();
        byte[] cipher = gcmData.getCiphertext().toByteArray();
        byte[] tag = gcmData.getTag().toByteArray();

        byte[] rss = rawECDHkey;
        byte[] dataAESGCM = AllNativeMethod.cdxtalkDecodeAESGCM(cipher, cipher.length,
                add, add.length, rss, rss.length, iv, iv.length, tag, tag.length);
        return dataAESGCM;
    }

    public static synchronized byte[] decodeAESGCM(byte[] rawECDHkey, Connect.GcmData gcmData) {
        byte[] aad = gcmData.getAad().toByteArray();
        byte[] iv = gcmData.getIv().toByteArray();
        byte[] cipher = gcmData.getCiphertext().toByteArray();
        byte[] tag = gcmData.getTag().toByteArray();

        byte[] rss = rawECDHkey;
        byte[] dataAESGCM = AllNativeMethod.cdxtalkDecodeAESGCM(cipher, cipher.length,
                aad, aad.length, rss, rss.length, iv, iv.length, tag, tag.length);
        return dataAESGCM;
    }
}