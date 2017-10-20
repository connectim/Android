package connect.activity.chat.model.fileload;

import android.content.Context;
import android.os.AsyncTask;

import connect.activity.chat.inter.FileUpLoad;
import connect.database.MemoryDataManager;
import instant.bean.ChatMsgEntity;
import instant.sender.model.BaseChat;
import instant.utils.cryption.EncryptionUtil;
import instant.utils.cryption.SupportKeyUril;
import protos.Connect;

/**
 * Created by gtq on 2016/12/5.
 */
public class VoiceUpload extends FileUpLoad {

    public VoiceUpload(Context context, BaseChat baseChat, ChatMsgEntity entity, FileUpListener listener) {
        this.context = context;
        this.context = context;
        this.baseChat = baseChat;
        this.msgExtEntity = entity;
        this.fileUpListener = listener;
    }

    @Override
    public void fileHandle() {
        super.fileHandle();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Connect.VoiceMessage voiceMessage = Connect.VoiceMessage.parseFrom(msgExtEntity.getContents());

                    String pubkey = SupportKeyUril.getPubKeyFromPriKey(MemoryDataManager.getInstance().getPriKey());
                    String priKey = MemoryDataManager.getInstance().getPriKey();

                    if (baseChat.chatType() != 2) {
                        Connect.GcmData gcmData = encodeAESGCMStructData(voiceMessage.getUrl());
                        Connect.RichMedia richMedia = Connect.RichMedia.newBuilder().
                                setEntity(gcmData.toByteString()).build();

                        gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.SALT, priKey, richMedia.toByteString());
                        mediaFile = Connect.MediaFile.newBuilder().setPubKey(pubkey).setCipherData(gcmData).build();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                localEncryptionSuccess(msgExtEntity);
                if (mediaFile != null) {
                    fileUp();
                }
            }
        }.execute();
    }

    @Override
    public void fileUp() {
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
