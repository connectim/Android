package connect.utils.chatfile.upload;

import android.content.Context;
import android.os.AsyncTask;

import com.google.protobuf.ByteString;

import java.io.File;

import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import connect.utils.chatfile.inter.BaseFileUp;
import connect.utils.chatfile.inter.FileUploadListener;
import instant.bean.ChatMsgEntity;
import instant.bean.Session;
import instant.sender.model.BaseChat;
import protos.Connect;

/**
 * Created by gtq on 2016/12/5.
 */
public class PhotoUpload extends BaseFileUp {

    private static String TAG = "_PhotoUpload";

    public PhotoUpload(Context context, BaseChat baseChat, ChatMsgEntity entity, FileUploadListener listener) {
        super();
        this.context = context;
        this.baseChat = baseChat;
        this.msgExtEntity = entity;
        this.fileUpListener = listener;
    }

    @Override
    public void startUpload() {
        super.startUpload();
        fileCompress();
    }

    private String thumbCompressFile;
    private String sourceCompressFile;

    @Override
    public void fileCompress() {
        super.fileCompress();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Connect.PhotoMessage photoMessage = Connect.PhotoMessage.parseFrom(msgExtEntity.getContents());
                    String filePath = photoMessage.getThum();

                    File firstFile = BitmapUtil.getInstance().compress(filePath);
                    File secondFile = BitmapUtil.getInstance().compress(firstFile.getAbsolutePath());
                    thumbCompressFile = firstFile.getAbsolutePath();
                    sourceCompressFile = secondFile.getAbsolutePath();

                    fileEncrypt();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                fileUpload();
            }
        }.execute();
    }

    @Override
    public void fileEncrypt() {
        super.fileEncrypt();

        Connect.RichMedia richMedia = null;
        if (baseChat.chatType() == Connect.ChatType.CONNECT_SYSTEM_VALUE) {
            richMedia = Connect.RichMedia.newBuilder().
                    setThumbnail(ByteString.copyFrom(FileUtil.filePathToByteArray(thumbCompressFile))).
                    setEntity(ByteString.copyFrom(FileUtil.filePathToByteArray(sourceCompressFile))).build();
        } else {
            byte[] thumbnailFileByte = FileUtil.filePathToByteArray(thumbCompressFile);
            ByteString thumbnailFileBytes = ByteString.copyFrom(thumbnailFileByte);
            byte[] sourceFileByte = FileUtil.filePathToByteArray(sourceCompressFile);
            ByteString sourceFileBytes = ByteString.copyFrom(sourceFileByte);

            richMedia = Connect.RichMedia.newBuilder()
                    .setThumbnail(thumbnailFileBytes)
                    .setEntity(sourceFileBytes)
                    .build();
        }

        Connect.StructData structData = Connect.StructData.newBuilder()
                .setPlainData(richMedia.toByteString())
                .build();

        String uid = Session.getInstance().getConnectCookie().getUid();
        String token = Session.getInstance().getChatCookie().getToken();
        mediaFile = Connect.MediaFile.newBuilder()
                .setUid(uid)
                .setToken(token)
                .setBody(structData.toByteString())
                .build();

        FileUtil.deleteFile(thumbCompressFile);
        FileUtil.deleteFile(sourceCompressFile);
    }

    @Override
    public void fileUpload() {
        super.fileUpload();
        resultUpFile(mediaFile, new FileResult() {

            @Override
            public void resultUpUrl(Connect.FileData mediaFile) {
                String thumb = getThumbUrl(mediaFile.getUrl(), mediaFile.getToken());
                String url = getUrl(mediaFile.getUrl(), mediaFile.getToken());

                try {
                    Connect.PhotoMessage photoMessage = Connect.PhotoMessage.parseFrom(msgExtEntity.getContents());
                    photoMessage = photoMessage.toBuilder()
                            .setThum(thumb)
                            .setUrl(url)
                            .setFileKey(ByteString.copyFrom(getRandomNumber()))
                            .build();

                    msgExtEntity = (ChatMsgEntity) msgExtEntity.clone();
                    msgExtEntity.setContents(photoMessage.toByteArray());
                    uploadSuccess(msgExtEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
