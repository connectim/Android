package connect.activity.chat.view.holder;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;

import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.view.BubbleImg;
import connect.database.green.DaoHelper.MessageHelper;
import connect.ui.activity.R;
import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import connect.utils.StringUtil;
import connect.utils.TimeUtil;
import connect.utils.ToastEUtil;
import connect.widget.selefriend.SelectRecentlyChatActivity;
import instant.bean.ChatMsgEntity;
import instant.bean.MsgDirect;
import protos.Connect;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgImgHolder extends MsgChatHolder {

    private BubbleImg imgmsg;
    private String filePath;
    private MediaScannerConnection scanner = null;
    private Connect.PhotoMessage photoMessage;

    public MsgImgHolder(View itemView) {
        super(itemView);
        imgmsg = (BubbleImg) itemView.findViewById(R.id.imgmsg);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final ChatMsgEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        photoMessage = Connect.PhotoMessage.parseFrom(msgExtEntity.getContents());

        Connect.ChatType chatType = Connect.ChatType.forNumber(msgExtEntity.getChatType());
        String url = !TextUtils.isEmpty(photoMessage.getThum()) ? photoMessage.getThum() : photoMessage.getUrl();
        String hexString = StringUtil.bytesToHexString(photoMessage.getFileKey().toByteArray());
        imgmsg.loadUri(msgExtEntity.parseDirect(), chatType, msgExtEntity.getMessage_ower(), msgExtEntity.getMessage_id(), hexString,url, photoMessage.getImageWidth(), photoMessage.getImageHeight());

        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String thumb = photoMessage.getThum();
                String path = FileUtil.isLocalFile(thumb) ? thumb : FileUtil.newContactFileName(msgExtEntity.getMessage_ower(), msgExtEntity.getMessage_id(), FileUtil.FileType.IMG);
                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.IMGVIEWER, path);

                if ((msgExtEntity.getSnap_time() == null || msgExtEntity.getSnap_time() == 0) && msgExtEntity.parseDirect() == MsgDirect.From) {
                    msgExtEntity.setSnap_time(TimeUtil.getCurrentTimeInLong());
                    MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                }
            }
        });
    }

    @Override
    public String[] longPressPrompt() {
        return context.getResources().getStringArray(R.array.prompt_img);
    }

    @Override
    public void saveInPhone() {
        super.saveInPhone();
        scanner = new MediaScannerConnection(context, new MediaScannerConnection.MediaScannerConnectionClient() {
            @Override
            public void onMediaScannerConnected() {
                if (filePath != null) {
                    scanner.scanFile(filePath, "media/*");
                }
            }

            @Override
            public void onScanCompleted(String path, Uri uri) {
                scanner.disconnect();
            }
        });

        String url = imgmsg.getLocalPath();
        Glide.with(context).load(url).asBitmap().into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                File file = BitmapUtil.getInstance().bitmapSavePathDCIM(resource);
                filePath = file.getAbsolutePath();
                try {
                    scanner.connect();
                    ToastEUtil.makeText(context, R.string.Login_Save_successful).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                String errorMsg = e.getMessage();
                if (!TextUtils.isEmpty(errorMsg)) {
                    ToastEUtil.makeText(context,errorMsg).show();
                }
            }
        });
    }

    @Override
    public void transPondTo() {
        super.transPondTo();
        String url = TextUtils.isEmpty(photoMessage.getThum()) ? photoMessage.getThum() : photoMessage.getUrl();
        String localPath = FileUtil.newContactFileName(getMsgExtEntity().getMessage_ower(),
                getMsgExtEntity().getMessage_id(), FileUtil.FileType.IMG);

        if (FileUtil.isLocalFile(url) || FileUtil.isExistFilePath(localPath)) {
            String local = FileUtil.isLocalFile(url) ? url : localPath;
            SelectRecentlyChatActivity.startActivity((Activity) context, SelectRecentlyChatActivity.TRANSPOND, String.valueOf(getMsgExtEntity().getMessageType()), local,photoMessage.getImageWidth(),photoMessage.getImageHeight());
        }
    }
}
