package connect.utils.chatfile.upload;

import android.content.Context;
import android.os.AsyncTask;

import com.google.protobuf.ByteString;

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
public class VoiceUpload extends BaseFileUp {

    private static String TAG = "_VoiceUpload";

    public VoiceUpload(Context context, BaseChat baseChat, ChatMsgEntity entity, FileUploadListener listener) {
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

    private String sourceCompressFile;

    @Override
    public void fileCompress() {
        super.fileCompress();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Connect.VoiceMessage voiceMessage = Connect.VoiceMessage.parseFrom(msgExtEntity.getContents());
                    sourceCompressFile = voiceMessage.getUrl();

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

        byte[] sourceFileByte = FileUtil.filePathToByteArray(sourceCompressFile);
        ByteString sourceFileBytes = ByteString.copyFrom(sourceFileByte);

        Connect.RichMedia richMedia = Connect.RichMedia.newBuilder().
                setEntity(sourceFileBytes)
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
                String url = getUrl(mediaFile.getUrl(), mediaFile.getToken());

                try {
                    Connect.VoiceMessage voiceMessage = Connect.VoiceMessage.parseFrom(msgExtEntity.getContents());
                    voiceMessage = voiceMessage.toBuilder()
                            .setUrl(url)
                            .setFileKey(ByteString.copyFrom(getRandomNumber()))
                            .build();

                    msgExtEntity = (ChatMsgEntity) msgExtEntity.clone();
                    msgExtEntity.setContents(voiceMessage.toByteArray());
                    uploadSuccess(msgExtEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
