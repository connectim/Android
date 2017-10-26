package connect.activity.chat.inter;

import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupEntity;
import connect.utils.StringUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.okhttp.DownLoadFile;
import protos.Connect;

/**
 * Created by gtq on 2016/12/7.
 */
public class FileDownLoad {
    private static FileDownLoad fileDownLoad;

    public static FileDownLoad getInstance() {
        if (fileDownLoad == null) {
            fileDownLoad = new FileDownLoad();
        }
        return fileDownLoad;
    }

    public void downChatFile(final Connect.ChatType roomType, String url, final String pukkey, final IFileDownLoad fileDownLoad) {
        DownLoadFile downLoadFile = new DownLoadFile(url, new DownLoadFile.ResultListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                fileDownLoad.onProgress(bytesRead, contentLength);
            }

            @Override
            public void fail() {
                fileDownLoad.failDown();
            }

            @Override
            public void success(byte[] data) {
                try {
                    if (roomType == Connect.ChatType.CONNECT_SYSTEM) {
                        fileDownLoad.successDown(data);
                    } else {
                        Connect.GcmData gcmData = Connect.GcmData.parseFrom(data);
                        Connect.StructData structData = null;
                        if (roomType == Connect.ChatType.PRIVATE) {//private chat
                            structData = DecryptionUtil.decodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY, SharedPreferenceUtil.getInstance().getUser().getPriKey(), pukkey, gcmData);
                        } else if (roomType == Connect.ChatType.GROUPCHAT) {//group chat
                            GroupEntity groupEntity= ContactHelper.getInstance().loadGroupEntity(pukkey);
                            structData = DecryptionUtil.decodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY, StringUtil.hexStringToBytes(groupEntity.getEcdh_key()), gcmData);
                        }
                        byte[] dataFile = structData.getPlainData().toByteArray();
                        fileDownLoad.successDown(dataFile);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        downLoadFile.downFile();
    }

    public interface IFileDownLoad {

        void successDown(byte[] bytes);

        void failDown();

        void onProgress(long bytesWritten, long totalSize);
    }
}
