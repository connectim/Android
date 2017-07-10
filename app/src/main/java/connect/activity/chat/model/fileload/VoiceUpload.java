package connect.activity.chat.model.fileload;

import android.content.Context;
import android.os.AsyncTask;

import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.MessageHelper;
import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.inter.FileUpLoad;
import connect.activity.chat.model.content.BaseChat;
import connect.utils.FileUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import protos.Connect;

/**
 * Created by gtq on 2016/12/5.
 */
public class VoiceUpload extends FileUpLoad {

    public VoiceUpload(Context context, BaseChat baseChat, MsgDefinBean bean, FileUpListener listener) {
        this.context = context;
        this.context = context;
        this.baseChat = baseChat;
        this.bean = bean;
        this.fileUpListener = listener;
    }

    @Override
    public void fileHandle() {
        super.fileHandle();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    bean.setExt1(FileUtil.fileSize(bean.getContent()));
                    MessageHelper.getInstance().insertToMsg(bean);

                    String pubkey = SupportKeyUril.getPubKeyFromPriKey(MemoryDataManager.getInstance().getPriKey());
                    String priKey = MemoryDataManager.getInstance().getPriKey();

                    if (baseChat.roomType() != 2) {
                        Connect.GcmData gcmData = encodeAESGCMStructData(bean.getContent());
                        Connect.RichMedia richMedia = Connect.RichMedia.newBuilder().
                                setEntity(gcmData.toByteString()).build();

                    gcmData = EncryptionUtil.encodeAESGCMStructData(SupportKeyUril.EcdhExts.SALT, priKey, richMedia.toByteString());
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
                msgEntity = (MsgEntity) baseChat.voiceMsg(bean.getContent(), bean.getSize(), bean.getExt1());
                msgEntity.getMsgDefinBean().setMessage_id(bean.getMessage_id());
                localEncryptionSuccess(msgEntity);

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
                String content = getUrl(mediaFile.getUrl(), mediaFile.getToken());
                MsgEntity index = (MsgEntity) baseChat.voiceMsg(content, bean.getSize(), bean.getExt1());
                index.getMsgDefinBean().setMessage_id(bean.getMessage_id());

                uploadSuccess(index);
            }
        });
    }
}
