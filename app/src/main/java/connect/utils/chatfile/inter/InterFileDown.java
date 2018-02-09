package connect.utils.chatfile.inter;

import com.google.protobuf.InvalidProtocolBufferException;

import connect.utils.StringUtil;
import instant.utils.cryption.DecryptionUtil;
import instant.utils.cryption.EncryptionUtil;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/31.
 */

public abstract class InterFileDown {

    public abstract void successDown(byte[] bytes);

    public abstract void failDown();

    public abstract void onProgress(long bytesWritten, long totalSize);

    public byte[] decodeFile(String fileKey, byte[] fileBytes) {
        byte[] decodeBytes = fileBytes;
        try {
            Connect.GcmData gcmData = Connect.GcmData.parseFrom(fileBytes);
            byte[] ecdhExts =StringUtil.hexStringToBytes(fileKey);
            decodeBytes = DecryptionUtil.decodeAESGCM(ecdhExts, gcmData);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return decodeBytes;
    }
}
