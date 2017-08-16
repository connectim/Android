package connect.activity.chat.view.holder;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;
import java.io.FileNotFoundException;

import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.view.BubbleImg;
import connect.activity.common.bean.ConverType;
import connect.activity.common.selefriend.ConversationActivity;
import connect.database.green.DaoHelper.MessageHelper;
import connect.ui.activity.R;
import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import connect.utils.ToastEUtil;
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
    public void buildRowData(MsgBaseHolder msgBaseHolder, final MsgExtEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        photoMessage = Connect.PhotoMessage.parseFrom(msgExtEntity.getContents());

        Connect.ChatType chatType = Connect.ChatType.forNumber(msgExtEntity.getChatType());
        String url = TextUtils.isEmpty(photoMessage.getThum()) ? photoMessage.getThum() : photoMessage.getUrl();
        imgmsg.setOpenBurn(photoMessage.getSnapTime() > 0);
        imgmsg.loadUri(msgExtEntity.parseDirect(), chatType,msgExtEntity.getMessage_ower(), msgExtEntity.getMessage_id(), url,photoMessage.getImageWidth(),photoMessage.getImageHeight());

        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String thumb = photoMessage.getThum();
                String path = FileUtil.islocalFile(thumb) ? thumb : FileUtil.newContactFileName(msgExtEntity.getMessage_ower(), msgExtEntity.getMessage_id(), FileUtil.FileType.IMG);
                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.IMGVIEWER, path);

                if (msgExtEntity.getRead_time() == 0 && msgExtEntity.parseDirect() == MsgDirect.From) {
                    RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.BURNMSG_READ, msgExtEntity.getMessage_id(), msgExtEntity.parseDirect());
                    MessageHelper.getInstance().updateMsgState(msgExtEntity.getMessage_id(), 2);
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
                    MediaStore.Images.Media.insertImage(context.getContentResolver(), filePath, "", null);
                    scanner.connect();
                    ToastEUtil.makeText(context, R.string.Login_Save_successful).show();
                } catch (FileNotFoundException e) {
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

        if (FileUtil.islocalFile(url) || FileUtil.isExistFilePath(localPath)) {
            String local = FileUtil.islocalFile(url) ? url : localPath;
            ConversationActivity.startActivity((Activity) context, ConverType.TRANSPOND, String.valueOf(getMsgExtEntity().getMessageType()), local);
        }
    }
}
