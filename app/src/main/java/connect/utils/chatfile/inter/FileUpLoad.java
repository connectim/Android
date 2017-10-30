package connect.utils.chatfile.inter;

import android.content.Context;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.MessageHelper;
import connect.instant.inter.ConversationListener;
import connect.ui.activity.R;
import connect.utils.FileUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.StringUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.log.LogManager;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.ResultCall;
import instant.bean.ChatMsgEntity;
import instant.sender.model.BaseChat;
import instant.sender.model.GroupChat;
import instant.utils.manager.FailMsgsManager;
import protos.Connect;

/**
 * Created by gtq on 2016/12/5.
 */
public abstract class FileUpLoad {

    private String Tag = "_FileUpLoad";
    protected Context context;
    protected ChatMsgEntity msgExtEntity;
    protected BaseChat baseChat;
    protected Connect.MediaFile mediaFile;

    public void fileHandle() {
        if (baseChat.chatType() != Connect.ChatType.CONNECT_SYSTEM_VALUE) {
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
        String priKey = SharedPreferenceUtil.getInstance().getUser().getPriKey();

        byte[] fileSie = FileUtil.filePathToByteArray(filePath);
        ByteString fileBytes = ByteString.copyFrom(fileSie);
        LogManager.getLogger().d(Tag, "ByteString size:" + fileBytes.size());

        if (baseChat.chatType() == Connect.ChatType.PRIVATE_VALUE) {
            gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY, priKey, baseChat.chatKey(), fileBytes);
        } else if (baseChat.chatType() == Connect.ChatType.GROUPCHAT_VALUE) {
            gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY, StringUtil.hexStringToBytes(((GroupChat) baseChat).groupEcdh()), fileBytes);
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
    public void localEncryptionSuccess(ChatMsgEntity msgExtEntity) {
        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
        ((ConversationListener) baseChat).updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime());
    }

    /**
     * Send upload successful file information
     *
     * @param msgExtEntity
     */
    public void uploadSuccess(ChatMsgEntity msgExtEntity) {
        baseChat.sendPushMsg(msgExtEntity);
        fileUpListener.upSuccess(msgExtEntity.getMessage_id());
    }

    protected String getThumbUrl(String url, String token) {
        return url + "/thumb?pub_key=" + msgExtEntity.getMessage_ower() + "&token=" + token;
    }

    protected String getUrl(String url, String token) {
        return url + "?pub_key=" + msgExtEntity.getMessage_ower() + "&token=" + token;
    }

    protected FileUploadListener fileUpListener;

    public interface FileUploadListener {

        void upSuccess(String msgid);

        void uploadFail(int code, String message);
    }
}
