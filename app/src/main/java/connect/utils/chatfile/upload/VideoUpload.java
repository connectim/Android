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
import instant.bean.ChatMsgEntity;
import instant.bean.Session;
import instant.sender.model.BaseChat;
import protos.Connect;

/**
 * Created by gtq on 2016/12/5.
 */
public class VideoUpload extends BaseFileUp {

    private static String TAG = "_VideoUpload";

    public VideoUpload(Context context, BaseChat baseChat, ChatMsgEntity entity, FileUploadListener listener) {
        super();
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

        byte[] thumbnailFileByte = encodeAESGCMStructData(thumbCompressFile);
        byte[] sourceFileByte = encodeAESGCMStructData(sourceCompressFile);
        ByteString thumbnailFileBytes = ByteString.copyFrom(thumbnailFileByte);
        ByteString sourceFileBytes = ByteString.copyFrom(sourceFileByte);

        Connect.RichMedia richMedia = Connect.RichMedia.newBuilder()
                .setThumbnail(thumbnailFileBytes)
                .setEntity(sourceFileBytes)
                .build();

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
                    videoMessage = videoMessage.toBuilder()
                            .setCover(thumb)
                            .setUrl(url)
                            .setFileKey(ByteString.copyFrom(getRandomNumber()))
                            .build();

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
