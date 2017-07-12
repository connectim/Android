package connect.activity.chat.inter;

import connect.activity.chat.bean.RoomType;
import connect.database.MemoryDataManager;
import connect.activity.chat.bean.RoomSession;
import connect.utils.StringUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
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

    public void downChatFile(final RoomType roomType, String url, final String pukkey, final IFileDownLoad fileDownLoad) {
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
                    if (roomType == RoomType.RobotType) {
                        fileDownLoad.successDown(data);
                    } else {
                        Connect.GcmData gcmData = Connect.GcmData.parseFrom(data);
                        Connect.StructData structData = null;
                        if (roomType == RoomType.FriendType) {//private chat
                            structData = DecryptionUtil.decodeAESGCMStructData(SupportKeyUril.EcdhExts.EMPTY, MemoryDataManager.getInstance().getPriKey(), pukkey, gcmData);
                        } else if (roomType == RoomType.GroupType) {//group chat
                            structData = DecryptionUtil.decodeAESGCMStructData(SupportKeyUril.EcdhExts.EMPTY, StringUtil.hexStringToBytes(RoomSession.getInstance().getGroupEcdh()), gcmData);
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
