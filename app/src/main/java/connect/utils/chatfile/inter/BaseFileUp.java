package connect.utils.chatfile.inter;

import android.content.Context;
import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;

import connect.database.green.DaoHelper.MessageHelper;
import connect.instant.inter.ConversationListener;
import connect.ui.activity.R;
import connect.utils.FileUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.HttpRequest;
import connect.utils.okhttp.ResultCall;
import instant.bean.ChatMsgEntity;
import instant.sender.model.BaseChat;
import instant.utils.cryption.EncryptionUtil;
import instant.utils.cryption.SupportKeyUril;
import instant.utils.manager.FailMsgsManager;
import protos.Connect;

/**
 * Created by gtq on 2016/12/5.
 */
public abstract class BaseFileUp implements InterFileUp {

    private static String TAG = "_BaseFileUp";
    protected Context context;
    protected ChatMsgEntity msgExtEntity;
    protected BaseChat baseChat;
    protected Connect.MediaFile mediaFile;
    public FileUploadListener fileUpListener;

    private byte[] randomNumber = new byte[]{};

    public BaseFileUp() {
        randomNumber = SupportKeyUril.createSecureRandom(32);
    }

    public synchronized byte[] encodeAESGCMStructData(String filePath) {
        byte[] fileBytes = FileUtil.filePathToByteArray(filePath);
        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCM(EncryptionUtil.ExtendedECDH.NONE, randomNumber, fileBytes);
        fileBytes = gcmData.toByteArray();
        return fileBytes;
    }

    public void resultUpFile(Connect.MediaFile mediaFile, final FileResult fileResult) {
        HttpRequest.getInstance().post(UriUtil.UPLOAD_FILE, mediaFile.toByteArray(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.StructData structData = Connect.StructData.parseFrom(response.getBody());

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
                String errorMessage = response.getMessage();
                if (TextUtils.isEmpty(errorMessage)) {
                    errorMessage = context.getString(R.string.Network_equest_failed_please_try_again_later);
                }
                ToastEUtil.makeText(context, errorMessage, 2).show();
            }
        });
    }

    public interface FileResult {
        void resultUpUrl(Connect.FileData mediaFile);
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
        return url + "/thumb?token=" + token;
    }

    protected String getUrl(String url, String token) {
        return url + "?token=" + token;
    }


    @Override
    public void startUpload() {
        if (baseChat.chatType() != Connect.ChatType.CONNECT_SYSTEM_VALUE) {
            FailMsgsManager.getInstance().sendDelayFailMsg(msgExtEntity.getMessage_ower(), msgExtEntity.getMessage_id(), null, null);
        }
    }

    @Override
    public void fileCompress() {

    }

    @Override
    public void fileEncrypt() {
        MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
        ((ConversationListener) baseChat).updateRoomMsg(null, msgExtEntity.showContent(), msgExtEntity.getCreatetime());
    }

    @Override
    public void fileUpload() {

    }

    public byte[] getRandomNumber() {
        return randomNumber;
    }
}
