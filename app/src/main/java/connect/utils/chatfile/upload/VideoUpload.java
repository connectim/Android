package connect.utils.chatfile.upload;

import android.content.Context;
import android.os.AsyncTask;

import com.google.protobuf.ByteString;
import com.netcompss.ffmpeg4android.CommandValidationException;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.loader.LoadJNI;

import java.io.File;

import connect.utils.FileUtil;
import connect.utils.chatfile.inter.BaseFileUp;
import connect.utils.chatfile.inter.FileUploadListener;
import connect.utils.cryption.EncryptionUtil;
import instant.bean.ChatMsgEntity;
import instant.bean.UserCookie;
import instant.sender.model.BaseChat;
import protos.Connect;

/**
 * Created by gtq on 2016/12/5.
 */
public class VideoUpload extends BaseFileUp {

    private static String TAG = "_VideoUpload";

    public VideoUpload(Context context, BaseChat baseChat, ChatMsgEntity entity, FileUploadListener listener) {
        this.context = context;
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


    public String videoCompress(String filepath) {
        LoadJNI vk = new LoadJNI();
        try {
            // complex command
            //vk.run(complexCommand, workFolder, getApplicationContext());
            File tempFile = FileUtil.newTempFile(FileUtil.FileType.VIDEO);
            String commandStr = "ffmpeg -y -i " + filepath + " -strict experimental -s 160x120 -r 25 -vcodec mpeg4 -b 150k -ab 48000 -ac 2 -ar 22050 " + tempFile.getAbsolutePath();

            vk.run(GeneralUtils.utilConvertToComplex(commandStr), tempFile.getParent(), context);

            // running without command validation
            //vk.run(complexCommand, workFolder, getApplicationContext(), false);

            // copying vk.log (internal native log) to the videokit folder
            //GeneralUtils.copyFileToFolder(vkLogPath, demoVideoFolder);
            return tempFile.getAbsolutePath();
        } catch (CommandValidationException e) {
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return filepath;
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
                    Connect.VideoMessage videoMessage = Connect.VideoMessage.parseFrom(msgExtEntity.getContents());
                    thumbCompressFile = videoMessage.getCover();
                    sourceCompressFile = videoMessage.getUrl();

                    fileEncrypt();
//            sourceCompressFile = videoCompress(filePath);
//            thumbCompressFile.delete();
//            sourceCompressFile.delete();
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
        Connect.GcmData gcmData = null;
        if (baseChat.chatType() == Connect.ChatType.CONNECT_SYSTEM_VALUE) {
            richMedia = Connect.RichMedia.newBuilder().
                    setThumbnail(ByteString.copyFrom(FileUtil.filePathToByteArray(thumbCompressFile))).
                    setEntity(ByteString.copyFrom(FileUtil.filePathToByteArray(sourceCompressFile))).build();
        } else {
            Connect.GcmData firstGcmData = encodeAESGCMStructData(thumbCompressFile);
            Connect.GcmData secondGcmData = encodeAESGCMStructData(sourceCompressFile);

            richMedia = Connect.RichMedia.newBuilder().
                    setThumbnail(firstGcmData.toByteString()).
                    setEntity(secondGcmData.toByteString()).build();
        }

        UserCookie userCookie = loadUserCookie();
        String myPrivateKey = userCookie.getPriKey();
        String myPublicKey = userCookie.getPubKey();
        gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.SALT, myPrivateKey, richMedia.toByteString());
        mediaFile = Connect.MediaFile.newBuilder()
                .setPubKey(myPublicKey)
                .setCipherData(gcmData)
                .build();
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
                    Connect.VideoMessage videoMessage = Connect.VideoMessage.parseFrom(msgExtEntity.getContents());
                    videoMessage = videoMessage.toBuilder().setCover(thumb)
                            .setUrl(url).build();

                    msgExtEntity = (ChatMsgEntity) msgExtEntity.clone();
                    msgExtEntity.setContents(videoMessage.toByteArray());
                    uploadSuccess(msgExtEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
