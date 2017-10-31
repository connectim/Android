package connect.utils.chatfile.upload;

import android.content.Context;
import android.os.AsyncTask;

import connect.utils.chatfile.inter.BaseFileUp;
import connect.activity.login.bean.UserBean;
import connect.database.SharedPreferenceUtil;
import connect.utils.chatfile.inter.FileUploadListener;
import connect.utils.cryption.EncryptionUtil;
import instant.bean.ChatMsgEntity;
import instant.sender.model.BaseChat;
import protos.Connect;

/**
 * Created by gtq on 2016/12/5.
 */
public class VoiceUpload extends BaseFileUp {

    private String Tag = "_VoiceUpload";

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
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                fileEncrypt();
            }
        }.execute();
    }

    @Override
    public void fileEncrypt() {
        super.fileEncrypt();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                UserBean userBean = SharedPreferenceUtil.getInstance().getUser();
                if (baseChat.chatType() != Connect.ChatType.CONNECT_SYSTEM_VALUE) {
                    Connect.GcmData gcmData = encodeAESGCMStructData(sourceCompressFile);
                    Connect.RichMedia richMedia = Connect.RichMedia.newBuilder().
                            setEntity(gcmData.toByteString()).build();

                    gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.SALT, userBean.getPriKey(), richMedia.toByteString());
                    mediaFile = Connect.MediaFile.newBuilder().setPubKey(userBean.getPubKey()).setCipherData(gcmData).build();
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
    public void fileUpload() {
        super.fileUpload();
        resultUpFile(mediaFile, new FileResult() {
            @Override
            public void resultUpUrl(Connect.FileData mediaFile) {
                String url = getUrl(mediaFile.getUrl(), mediaFile.getToken());

                try {
                    Connect.VoiceMessage voiceMessage = Connect.VoiceMessage.parseFrom(msgExtEntity.getContents());
                    voiceMessage = voiceMessage.toBuilder().setUrl(url).build();

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
