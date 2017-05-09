package connect.utils.cryption;

import com.google.protobuf.ByteString;

import connect.utils.ConfigUtil;
import connect.wallet.jni.AllNativeMethod;
import connect.wallet.jni.GCMModel;
import protos.Connect;

/**
 * Encryption related methods
 * Created by gtq on 2016/11/30.
 */
public class EncryptionUtil {

    private static String Tag = "EncryptionUtil";

    /**
     * Random private key
     *
     * @return
     */
    public static String randomPriKey() {
        return AllNativeMethod.cdCreateNewPrivKey();
    }

    /**
     * Random public key
     *
     * @param key
     * @return
     */
    public static String randomPubKey(String key) {
        return AllNativeMethod.cdGetPubKeyFromPrivKey(key);
    }

    /*********************************************************************************************************
     *                                        STRUCT DATA
     *******************************************************************************************************/
    /**
     * Data encapsulation into StructData
     *
     * @param bytes
     * @return
     */
    public static Connect.StructData transStructData(ByteString bytes) {
        ByteString random = ByteString.copyFrom(SupportKeyUril.createrBinaryRandom());
        Connect.StructData structData = Connect.StructData.newBuilder()
                .setRandom(random)
                .setPlainData(bytes)
                .build();
        return structData;
    }

    /******************************************  The default extension type  ***************************************************/

    public static Connect.GcmData encodeAESGCMStructData(String prikey, ByteString bytes) {
        return encodeAESGCMStructData(prikey, ConfigUtil.getInstance().serverPubkey(), bytes);
    }

    public static Connect.GcmData encodeAESGCMStructData(String prikey, String ServerPubkey, ByteString bytes) {
        byte[] ecdhKey = SupportKeyUril.rawECDHkey(prikey, ServerPubkey);
        return encodeAESGCMStructData(SupportKeyUril.EcdhExts.SALT, ecdhKey, bytes);
    }

    public static Connect.GcmData encodeAESGCMStructData(byte[] ecdhbytes, ByteString bytes) {
        return encodeAESGCMStructData(SupportKeyUril.EcdhExts.SALT, ecdhbytes, bytes);
    }

    /******************************************  Specify the extension type  ***************************************************/

    public static Connect.GcmData encodeAESGCMStructData(SupportKeyUril.EcdhExts exts, String prikey, ByteString bytes) {
        return encodeAESGCMStructData(exts, prikey, ConfigUtil.getInstance().serverPubkey(), bytes);
    }

    public static Connect.GcmData encodeAESGCMStructData(SupportKeyUril.EcdhExts exts, String prikey, String ServerPubkey, ByteString bytes) {
        byte[] ecdhKey = SupportKeyUril.rawECDHkey(prikey, ServerPubkey);
        if(null == ecdhKey){
            return null;
        }
        return encodeAESGCMStructData(exts, ecdhKey, bytes);
    }

    public static Connect.GcmData encodeAESGCMStructData(SupportKeyUril.EcdhExts exts, byte[] ecdhbytes, ByteString bytes) {
        Connect.StructData structData = transStructData(bytes);
        return encodeAESGCM(exts, ecdhbytes, structData.toByteArray());
    }
    /*********************************************************************************************************
     *                                        GCM DATA
     *******************************************************************************************************/

    /******************************************  No extension type  ***************************************************/
    public static Connect.GcmData encodeAESGCM(String prikey, String ServerPubkey, byte[] encodes) {
        byte[] ecdhKey = SupportKeyUril.rawECDHkey(prikey, ServerPubkey);
        return encodeAESGCM(ecdhKey, encodes);
    }

    public static Connect.GcmData encodeAESGCM(byte[] ecdhKey, byte[] encodes) {
        return encodeAESGCM(SupportKeyUril.EcdhExts.SALT, ecdhKey, encodes);
    }

    /****************************************** Specify the extension type ***************************************************/
    public static Connect.GcmData encodeAESGCM(SupportKeyUril.EcdhExts exts, String prikey, String ServerPubkey, byte[] encodes) {
        byte[] ecdhKey = SupportKeyUril.rawECDHkey(prikey, ServerPubkey);
        return encodeAESGCM(exts, ecdhKey, encodes);
    }

    public static Connect.GcmData encodeAESGCM(SupportKeyUril.EcdhExts exts, byte[] ecdhKey, byte[] encodes) {
        //ecdhkey extension
        ecdhKey = SupportKeyUril.ecdhKeyExtends(exts, ecdhKey);

        byte[] ab = "ConnectEncrypted".getBytes();
        ByteString iv = ByteString.copyFrom(SupportKeyUril.cdJNISeed());
        byte[] ib = iv.toByteArray();

        GCMModel gc = AllNativeMethod.cdxtalkEncodeAESGCM(encodes, encodes.length,
                ab, ab.length, ecdhKey, ecdhKey.length, ib, ib.length);

        ByteString enc = ByteString.copyFrom(gc.encrypt);
        ByteString mytag = ByteString.copyFrom(gc.tag);

        Connect.GcmData gcmData = Connect.GcmData.newBuilder().setIv(iv).
                setAad(ByteString.copyFrom(ab)).setCiphertext(enc).setTag(mytag).build();
        return gcmData;
    }
}