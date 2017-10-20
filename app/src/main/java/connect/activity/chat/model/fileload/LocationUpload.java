package connect.activity.chat.model.fileload;

import android.content.Context;
import android.os.AsyncTask;

import com.google.protobuf.ByteString;

import java.io.File;

import connect.activity.chat.inter.FileUpLoad;
import connect.database.MemoryDataManager;
import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import instant.bean.ChatMsgEntity;
import instant.sender.model.BaseChat;
import instant.utils.cryption.EncryptionUtil;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/16.
 */

public class LocationUpload extends FileUpLoad {

    private String Tag = "PhotoUpload";

    public LocationUpload(Context context, BaseChat baseChat, ChatMsgEntity entity, FileUpListener listener) {
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
                    Connect.LocationMessage locationMessage = Connect.LocationMessage.parseFrom(msgExtEntity.getContents());
                    String filePath = locationMessage.getScreenShot();

                    File firstFile = BitmapUtil.getInstance().compress(filePath);
                    File secondFile = BitmapUtil.getInstance().compress(firstFile.getAbsolutePath());
                    String firstPath = firstFile.getAbsolutePath();
                    String secondPath = secondFile.getAbsolutePath();

                    String priKey = MemoryDataManager.getInstance().getPriKey();
                    String pubkey = MemoryDataManager.getInstance().getPubKey();

                    Connect.GcmData gcmData = null;
                    Connect.RichMedia richMedia = null;
                    if (baseChat.chatType() == 2) {
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

                    gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.SALT, priKey, richMedia.toByteString());
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
                localEncryptionSuccess(msgExtEntity);
                fileUp();
            }
        }.execute();
    }

    @Override
    public void fileUp() {
        resultUpFile(mediaFile, new FileResult() {
            @Override
            public void resultUpUrl(Connect.FileData mediaFile) {
                String thumb = getThumbUrl(mediaFile.getUrl(), mediaFile.getToken());
                String url = getUrl(mediaFile.getUrl(), mediaFile.getToken());

                try {
                    Connect.LocationMessage locationMessage = Connect.LocationMessage.parseFrom(msgExtEntity.getContents());
                    locationMessage = locationMessage.toBuilder().setScreenShot(thumb).build();

                    msgExtEntity = (ChatMsgEntity) msgExtEntity.clone();
                    msgExtEntity.setContents(locationMessage.toByteArray());
                    uploadSuccess(msgExtEntity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
