package connect.activity.chat.model.fileload;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.google.protobuf.ByteString;

import java.io.File;

import connect.database.MemoryDataManager;
import connect.im.bean.MsgType;
import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.inter.FileUpLoad;
import connect.activity.chat.model.content.BaseChat;
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
        super.fileHandle();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    String filePath = bean.getContent();

                    File firstFile = BitmapUtil.getInstance().compress(filePath);
                    File secondFile = BitmapUtil.getInstance().compress(firstFile.getAbsolutePath());
                    String firstPath = firstFile.getAbsolutePath();
                    String secondPath = secondFile.getAbsolutePath();

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    options.inSampleSize = 1;
                    BitmapFactory.decodeFile(filePath, options);
                    bean.setImageOriginWidth(options.outWidth);
                    bean.setImageOriginHeight(options.outHeight);
                    bean.setExt1(FileUtil.fileSize(firstPath));

                    String pubkey = MemoryDataManager.getInstance().getPubKey();
                    String priKey = MemoryDataManager.getInstance().getPriKey();

                    Connect.GcmData gcmData = null;
                    Connect.RichMedia richMedia = null;
                    if (baseChat.roomType() == 2) {
                        richMedia = Connect.RichMedia.newBuilder().
                                setThumbnail(ByteString.copyFrom(FileUtil.filePathToByteArray(firstPath))).
                                setEntity(ByteString.copyFrom(FileUtil.filePathToByteArray(secondPath))).build();
                    } else {
                        Connect.GcmData firstGcmData = encodeAESGCMStructData(firstPath);
                        Connect.GcmData secondGcmData = encodeAESGCMStructData(secondPath);
                        richMedia = Connect.RichMedia.newBuilder().
                                setThumbnail(firstGcmData.toByteString()).
                                setEntity(secondGcmData.toByteString()).build();
                    }

                    gcmData = EncryptionUtil.encodeAESGCMStructData(SupportKeyUril.EcdhExts.SALT,priKey, richMedia.toByteString());
                    mediaFile = Connect.MediaFile.newBuilder().setPubKey(pubkey).setCipherData(gcmData).build();

//                    firstFile.delete();
//                    secondFile.delete();
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
