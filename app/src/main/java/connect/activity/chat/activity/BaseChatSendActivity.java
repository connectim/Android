package connect.activity.chat.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.List;

import connect.activity.chat.bean.GeoAddressBean;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.bean.RoomSession;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ConversionSettingHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.DaoHelper.TransactionHelper;
import connect.ui.activity.R;
import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import connect.utils.TimeUtil;
import connect.utils.chatfile.inter.BaseFileUp;
import connect.utils.chatfile.inter.FileUploadListener;
import connect.utils.chatfile.upload.LocationUpload;
import connect.utils.chatfile.upload.PhotoUpload;
import connect.utils.chatfile.upload.VideoUpload;
import connect.utils.chatfile.upload.VoiceUpload;
import instant.bean.ChatMsgEntity;
import instant.bean.UserOrderBean;
import instant.sender.model.GroupChat;
import protos.Connect;

/**
 * Created by Administrator on 2017/10/31.
 */

public abstract class BaseChatSendActivity extends BaseChatReceiveActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * mssage send
     *
     * @param msgSend
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public synchronized void onEventMainThread(MsgSend msgSend) {
        String uid = null;
        String filePath = null;
        UserOrderBean userOrderBean = null;
        ChatMsgEntity chatMsgEntity = null;
        BaseFileUp upLoad = null;

        Object[] objects = null;
        if (msgSend.getObj() != null) {
            objects = (Object[]) msgSend.getObj();
        }

        switch (msgSend.getMsgType()) {
            case Text:
                String txt = (String) objects[0];
                if (normalChat.chatType() == Connect.ChatType.GROUPCHAT_VALUE) {
                    List<String> atList = (List<String>) objects[1];
                    chatMsgEntity = ((GroupChat) normalChat).groupTxtMsg(txt, atList);
                } else {
                    chatMsgEntity = normalChat.txtMsg(txt);
                }
                sendNormalMsg(true, chatMsgEntity);
                break;
            case Photo:
                List<String> paths = (List<String>) objects[0];
                for (String str : paths) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    options.inSampleSize = 1;
                    BitmapFactory.decodeFile(str, options);

                    chatMsgEntity = normalChat.photoMsg(str, str, FileUtil.fileSize(str), options.outWidth, options.outHeight);

                    adapterInsetItem(chatMsgEntity);
                    upLoad = new PhotoUpload(activity, normalChat, chatMsgEntity, new FileUploadListener() {
                        @Override
                        public void upSuccess(String msgid) {
                        }

                        @Override
                        public void uploadFail(int code, String message) {

                        }
                    });
                    upLoad.startUpload();
                }
                break;
            case Voice:
                chatMsgEntity = normalChat.voiceMsg((String) objects[0], (Integer) objects[1]);

                adapterInsetItem(chatMsgEntity);
                upLoad = new VoiceUpload(activity, normalChat, chatMsgEntity, new FileUploadListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }

                    @Override
                    public void uploadFail(int code, String message) {

                    }
                });
                upLoad.startUpload();
                break;
            case Emotion:
                chatMsgEntity = normalChat.emotionMsg((String) objects[0]);
                sendNormalMsg(true, chatMsgEntity);
                break;
            case Video:
                filePath = (String) objects[0];
                Bitmap thumbBitmap = BitmapUtil.thumbVideo(filePath);
                File thumbFile = BitmapUtil.getInstance().bitmapSavePath(thumbBitmap);
                chatMsgEntity = normalChat.videoMsg(thumbFile.getAbsolutePath(), filePath, (Integer) objects[1],
                        FileUtil.fileSizeOf(filePath), thumbBitmap.getWidth(), thumbBitmap.getHeight());

                adapterInsetItem(chatMsgEntity);
                upLoad = new VideoUpload(activity, normalChat, chatMsgEntity, new FileUploadListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }

                    @Override
                    public void uploadFail(int code, String message) {

                    }
                });
                upLoad.startUpload();
                break;
            case Name_Card:
                chatMsgEntity = normalChat.cardMsg((String) objects[0], (String) objects[1], (String) objects[2]);
                sendNormalMsg(true, chatMsgEntity);
                break;
            case BURNREAD_RECEIPT:
                String messageId = (String) objects[0];

                uid = SharedPreferenceUtil.getInstance().getUser().getUid();
                userOrderBean = new UserOrderBean();
                userOrderBean.burnReadReceipt(uid, chatIdentify, messageId);
                break;
            case Request_Payment:
                int payType = (int) objects[0];
                String payHashId = (String) objects[1];
                long payAmount = (long) objects[2];
                int payMembers = (int) objects[3];
                String payTips = (String) objects[4];

                chatMsgEntity = normalChat.paymentMsg(payType, payHashId, payAmount, payMembers, payTips);
                sendNormalMsg(true, chatMsgEntity);
                //add payment information
                TransactionHelper.getInstance().updateTransEntity(payHashId, chatMsgEntity.getMessage_id(), 0, payMembers);
                break;
            case Transfer:
                int transferType = (int) objects[0];
                String transferHashId = (String) objects[1];
                long transferAmount = (long) objects[2];
                String transferTips = (String) objects[3];

                chatMsgEntity = normalChat.transferMsg(transferType, transferHashId, transferAmount, transferTips);
                sendNormalMsg(true, chatMsgEntity);

                //add payment information
                TransactionHelper.getInstance().updateTransEntity((String) objects[0], chatMsgEntity.getMessage_id(), 1);
                break;
            case Location:
                GeoAddressBean geoAddress = (GeoAddressBean) objects[0];

                chatMsgEntity = normalChat.locationMsg((float) geoAddress.getLocationLatitude(), (float) geoAddress.getLocationLongitude(),
                        geoAddress.getAddress(), geoAddress.getPath(), geoAddress.getImageOriginWidth(), geoAddress.getImageOriginHeight());

                adapterInsetItem(chatMsgEntity);
                upLoad = new LocationUpload(activity, normalChat, chatMsgEntity, new FileUploadListener() {
                    @Override
                    public void upSuccess(String msgid) {
                    }

                    @Override
                    public void uploadFail(int code, String message) {

                    }
                });
                upLoad.startUpload();
                break;
            case Lucky_Packet:
                int luckyType = (int) objects[0];
                String luckyHashId = (String) objects[1];
                String luckyTips = (String) objects[2];
                long luckyAmount = (long) objects[3];

                chatMsgEntity = normalChat.luckPacketMsg(luckyType, luckyHashId, luckyAmount,luckyTips);
                sendNormalMsg(true, chatMsgEntity);
                break;
            case OUTER_WEBSITE:
                String webUrl = (String) objects[0];
                String webTitle = (String) objects[1];
                String webSubtitle = (String) objects[2];
                String webImage = (String) objects[3];

                chatMsgEntity = normalChat.outerWebsiteMsg(webUrl, webTitle, webSubtitle, webImage);
                sendNormalMsg(true, chatMsgEntity);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
