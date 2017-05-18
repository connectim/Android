package connect.ui.activity.chat.model.fileload;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.protobuf.ByteString;

import connect.db.MemoryDataManager;
import connect.db.SharedPreferenceUtil;
import connect.db.green.DaoHelper.MessageHelper;
import connect.im.bean.MsgType;
import connect.im.model.ChatSendManager;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.inter.FileUpLoad;
import connect.ui.activity.chat.model.content.BaseChat;
import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import protos.Connect;

/**
 * Created by gtq on 2016/12/5.
 */
public class PhotoUpload extends FileUpLoad {

    private String Tag = "PhotoUpload";

    public PhotoUpload(Context context, BaseChat baseChat, MsgDefinBean bean, FileUpListener listener) {
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
                    String filePath = bean.getContent();
                /*String comFist = BitmapUtil.bitmapToString(filePath,BitmapUtil.bigWidth,BitmapUtil.bigHeight);
                String comSecond = BitmapUtil.bitmapToString(comFist);*/
                    String comFist = BitmapUtil.resizeImage(filePath, BitmapUtil.bigWidth);
                    String comSecond = BitmapUtil.resizeImage(comFist, BitmapUtil.smallWidth);

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(filePath, options);

                    bean.setImageOriginWidth(options.outWidth);
                    bean.setImageOriginHeight(options.outHeight);
                    bean.setExt1(FileUtil.fileSize(comSecond));
                    MessageHelper.getInstance().insertToMsg(bean);

                    //// TODO: 2017/1/21 Assemble the MediaFile should be split in two implementations
                    String pubkey = SupportKeyUril.getPubKeyFromPriKey();
                    String priKey = MemoryDataManager.getInstance().getPriKey();

                    Connect.GcmData gcmData = null;
                    Connect.RichMedia richMedia = null;
                    if (baseChat.roomType() == 2) {
                        richMedia = Connect.RichMedia.newBuilder().
                                setThumbnail(ByteString.copyFrom(FileUtil.filePathToByteArray(comFist))).
                                setEntity(ByteString.copyFrom(FileUtil.filePathToByteArray(comSecond))).build();
                    } else {
                        Connect.GcmData firstGcmData = encodeAESGCMStructData(comFist);
                        Connect.GcmData secondGcmData = encodeAESGCMStructData(comSecond);
                        richMedia = Connect.RichMedia.newBuilder().
                                setThumbnail(firstGcmData.toByteString()).
                                setEntity(secondGcmData.toByteString()).build();
                    }

                    gcmData = EncryptionUtil.encodeAESGCMStructData(SupportKeyUril.EcdhExts.SALT,priKey, richMedia.toByteString());
                    mediaFile = Connect.MediaFile.newBuilder().setPubKey(pubkey).setCipherData(gcmData).build();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                ChatSendManager.getInstance().sendDelayFailMsg(bean.getPublicKey(),bean.getMessage_id());
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
                String content = getThumbUrl(mediaFile.getUrl(), mediaFile.getToken());
                String url = getUrl(mediaFile.getUrl(), mediaFile.getToken());

                if (MsgType.toMsgType(bean.getType()) == MsgType.Photo) {
                    fileUpListener.upSuccess(bean.getMessage_id(), content, url, bean.getExt1(), bean.getImageOriginWidth(), bean.getImageOriginHeight());
                } else if (MsgType.toMsgType(bean.getType()) == MsgType.Location) {
                    fileUpListener.upSuccess(bean.getMessage_id(), content, bean.getLocationExt(), bean.getImageOriginWidth(), bean.getImageOriginHeight());
                }
            }
        });
    }
}
