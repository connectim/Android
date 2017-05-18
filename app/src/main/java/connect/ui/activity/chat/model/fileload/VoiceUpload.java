package connect.ui.activity.chat.model.fileload;

import android.content.Context;
import android.os.AsyncTask;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.MessageHelper;
import connect.im.model.ChatSendManager;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.inter.FileUpLoad;
import connect.ui.activity.chat.model.content.BaseChat;
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
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    bean.setExt1(FileUtil.fileSize(bean.getContent()));
                    MessageHelper.getInstance().insertToMsg(bean);

                    String pubkey = SupportKeyUril.getPubKeyFromPriKey(MemoryDataManager.getInstance().getPriKey());
                    String priKey = MemoryDataManager.getInstance().getPriKey();

                    Connect.GcmData gcmData = null;
                    Connect.RichMedia richMedia = null;
                    if (baseChat.roomType() == 2) {
                        Connect.GcmData firstGcmData = encodeAESGCMStructData(bean.getContent());
                        richMedia = Connect.RichMedia.newBuilder().
                                setEntity(firstGcmData.toByteString()).build();
                    } else {
                        gcmData = encodeAESGCMStructData(bean.getContent());
                        richMedia = Connect.RichMedia.newBuilder().
                                setEntity(gcmData.toByteString()).build();
                    }

                    gcmData = EncryptionUtil.encodeAESGCMStructData(SupportKeyUril.EcdhExts.SALT, priKey, richMedia.toByteString());
                    mediaFile = Connect.MediaFile.newBuilder().setPubKey(pubkey).setCipherData(gcmData).build();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                ChatSendManager.getInstance().sendDelayFailMsg(bean.getPublicKey(), bean.getMessage_id());
                fileUp();
            }
        }.execute();
    }

    @Override
    public void fileUp() {
        if (mediaFile == null) {
            return;
        }
        ResultUpFile(mediaFile, new FileResult() {
            @Override
            public void resultUpUrl(Connect.FileData mediaFile) {
                String content = getUrl(mediaFile.getUrl(), mediaFile.getToken());
                fileUpListener.upSuccess(bean.getMessage_id(), content, bean.getSize(), bean.getExt1());
            }
        });
    }
}
