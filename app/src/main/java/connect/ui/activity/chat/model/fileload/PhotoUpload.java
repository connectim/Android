package connect.ui.activity.chat.model.fileload;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.protobuf.ByteString;

import connect.db.SharedPreferenceUtil;
import connect.im.bean.MsgType;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.inter.FileUpLoad;
import connect.ui.activity.chat.model.content.BaseChat;
import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import connect.utils.cryption.EncryptionUtil;
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
        super.fileHandle();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String filePath = bean.getContent();
                    String comFist = BitmapUtil.resizeImage(filePath, BitmapUtil.bigWidth);
                    String comSecond = BitmapUtil.resizeImage(comFist, BitmapUtil.smallWidth);

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(filePath, options);

                    bean.setImageOriginWidth(options.outWidth);
                    bean.setImageOriginHeight(options.outHeight);
                    bean.setExt1(FileUtil.fileSize(comSecond));

                    String pubkey = SharedPreferenceUtil.getInstance().getPubKey();
                    String priKey = SharedPreferenceUtil.getInstance().getPriKey();
                    Connect.GcmData gcmData = null;
                    Connect.RichMedia richMedia = null;
                    if (baseChat.roomType() == 2) {
                        richMedia = Connect.RichMedia.newBuilder().
                                setThumbnail(ByteString.copyFrom(FileUtil.filePathToByteArray(comFist))).
                                setEntity(ByteString.copyFrom(FileUtil.filePathToByteArray(comSecond))).build();
                    } else {
                        Connect.GcmData firstGcmData = encodeAESGCMStructData(comSecond);
                        Connect.GcmData secondGcmData = encodeAESGCMStructData(comFist);
                        richMedia = Connect.RichMedia.newBuilder().
                                setThumbnail(firstGcmData.toByteString()).
                                setEntity(secondGcmData.toByteString()).build();
                    }

                    gcmData = EncryptionUtil.encodeAESGCMStructData(priKey, richMedia.toByteString());
                    mediaFile = Connect.MediaFile.newBuilder().setPubKey(pubkey).setCipherData(gcmData).build();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if (MsgType.toMsgType(bean.getType()) == MsgType.Photo) {
                    msgEntity = (MsgEntity) baseChat.photoMsg(bean.getContent(), bean.getExt1());
                } else if (MsgType.toMsgType(bean.getType()) == MsgType.Location) {
                    msgEntity = (MsgEntity) baseChat.locationMsg(bean.getContent(), bean.getLocationExt());
                }
                msgEntity.getMsgDefinBean().setMessage_id(bean.getMessage_id());
                msgEntity.getMsgDefinBean().setImageOriginWidth(bean.getImageOriginWidth());
                msgEntity.getMsgDefinBean().setImageOriginHeight(bean.getImageOriginHeight());
                localEncryptionSuccess(msgEntity);

                fileUp();
            }
        }.execute();
    }

    @Override
    public void fileUp() {
        resultUpFile(mediaFile, new FileResult() {
            @Override
            public void resultUpUrl(Connect.FileData mediaFile) {
                String content = getThumbUrl(mediaFile.getUrl(), mediaFile.getToken());
                String url = getUrl(mediaFile.getUrl(), mediaFile.getToken());

                if (MsgType.toMsgType(bean.getType()) == MsgType.Photo) {
                    msgEntity = (MsgEntity) baseChat.photoMsg(content, bean.getExt1());
                    msgEntity.getMsgDefinBean().setUrl(url);
                } else if (MsgType.toMsgType(bean.getType()) == MsgType.Location) {
                    msgEntity = (MsgEntity) baseChat.locationMsg(content, bean.getLocationExt());
                }
                msgEntity.getMsgDefinBean().setMessage_id(bean.getMessage_id());
                msgEntity.getMsgDefinBean().setImageOriginWidth(bean.getImageOriginWidth());
                msgEntity.getMsgDefinBean().setImageOriginHeight(bean.getImageOriginHeight());

                uploadSuccess(msgEntity);
            }
        });
    }
}
