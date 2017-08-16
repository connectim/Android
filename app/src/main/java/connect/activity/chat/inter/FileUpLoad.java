package connect.activity.chat.inter;

import android.content.Context;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.model.content.BaseChat;
import connect.activity.chat.model.content.GroupChat;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.MessageHelper;
import connect.im.model.FailMsgsManager;
import connect.ui.activity.R;
import connect.utils.FileUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.StringUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by gtq on 2016/12/5.
 */
public abstract class FileUpLoad {

    protected Context context;
    protected MsgExtEntity msgExtEntity;
    protected BaseChat baseChat;
    protected Connect.MediaFile mediaFile;

    public void fileHandle() {
        if (baseChat.roomType() != 2) {
            FailMsgsManager.getInstance().sendDelayFailMsg(msgExtEntity.getMessage_ower(), msgExtEntity.getMessage_id(), null, null);
        }
    }

    public void fileUp() {
    }

    /**
     * File encryption
     *
     * @param filePath
     * @return
     */
    public Connect.GcmData encodeAESGCMStructData(String filePath) {
        Connect.GcmData gcmData = null;
        String priKey = MemoryDataManager.getInstance().getPriKey();

        byte[] fileSie = FileUtil.filePathToByteArray(filePath);
        if (baseChat.roomType() == 0) {
            gcmData = EncryptionUtil.encodeAESGCMStructData(SupportKeyUril.EcdhExts.EMPTY, priKey, baseChat.roomKey(), ByteString.copyFrom(fileSie));
        } else if (baseChat.roomType() == 1) {
            gcmData = EncryptionUtil.encodeAESGCMStructData(SupportKeyUril.EcdhExts.EMPTY, StringUtil.hexStringToBytes(((GroupChat) baseChat).groupEcdh()), ByteString.copyFrom(fileSie));
        }
        return gcmData;
    }

    public void resultUpFile(Connect.MediaFile mediaFile, final FileResult fileResult) {
        HttpRequest.getInstance().post(UriUtil.UPLOAD_FILE, mediaFile, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.FileData fileData = Connect.FileData.parseFrom(structData.getPlainData());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(fileData)) {
                        fileResult.resultUpUrl(fileData);
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(context, context.getString(R.string.Network_equest_failed_please_try_again_later), 2).show();
            }
        });
    }

    public interface FileResult {
        void resultUpUrl(Connect.FileData mediaFile);
    }

    /**
     * saves Local encryption information
     *
     * @param msgExtEntity
     */
    public void localEncryptionSuccess(MsgExtEntity msgExtEntity) {
        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
        baseChat.updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime());
    }

    /**
     * Send upload successful file information
     *
     * @param msgExtEntity
     */
    public void uploadSuccess(MsgExtEntity msgExtEntity) {
        baseChat.sendPushMsg(msgExtEntity);
        fileUpListener.upSuccess(msgExtEntity.getMessage_id());
    }

    protected String getThumbUrl(String url, String token) {
        return url + "/thumb?pub_key=" + msgExtEntity.getMessage_ower() + "&token=" + token;
    }

    protected String getUrl(String url, String token) {
        return url + "?pub_key=" + msgExtEntity.getMessage_ower() + "&token=" + token;
    }

    protected FileUpListener fileUpListener;

    public interface FileUpListener {
        void upSuccess(String msgid);
    }
}
