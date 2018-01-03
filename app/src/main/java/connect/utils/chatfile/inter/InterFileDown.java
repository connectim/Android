package connect.utils.chatfile.inter;

import com.google.protobuf.InvalidProtocolBufferException;

import instant.bean.Session;
import instant.bean.UserCookie;
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

    public byte[] decodeFile(String friendPubicKey, byte[] fileBytes) {
        byte[] decodeBytes = fileBytes;
        try {
            UserCookie userCookie = Session.getInstance().getConnectCookie();
            String myPrivateKey = userCookie.getPrivateKey();

            Connect.GcmData gcmData = Connect.GcmData.parseFrom(fileBytes);
            Connect.StructData structData = null;
            structData = DecryptionUtil.decodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY, myPrivateKey, friendPubicKey, gcmData);
            decodeBytes = structData.getPlainData().toByteArray();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return decodeBytes;
    }
}
