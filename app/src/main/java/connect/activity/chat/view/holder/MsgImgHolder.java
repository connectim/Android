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

import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.view.BubbleImg;
import connect.activity.common.bean.ConverType;
import connect.activity.common.selefriend.ConversationActivity;
import connect.database.green.DaoHelper.MessageHelper;
import connect.ui.activity.R;
import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import connect.utils.ToastEUtil;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgImgHolder extends MsgChatHolder {

    private BubbleImg imgmsg;
    private String filePath;
    private MediaScannerConnection scanner = null;

    public MsgImgHolder(View itemView) {
        super(itemView);
        imgmsg = (BubbleImg) itemView.findViewById(R.id.imgmsg);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final MsgEntity entity) {
        super.buildRowData(msgBaseHolder, entity);
        MsgDefinBean bean = entity.getMsgDefinBean();
        String url = TextUtils.isEmpty(bean.getContent()) ? bean.getUrl() : bean.getContent();

        imgmsg.setOpenBurn(TextUtils.isEmpty(definBean.getExt())?false:true);
        imgmsg.loadUri(direct, entity.getRoomType(),entity.getPubkey(), bean.getMessage_id(), url,definBean.getImageOriginWidth(),definBean.getImageOriginHeight());

        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MsgDefinBean bean = entity.getMsgDefinBean();
                String thumb = bean.getContent();
                String path = FileUtil.islocalFile(thumb) ? thumb : FileUtil.newContactFileName(entity.getPubkey(), bean.getMessage_id(), FileUtil.FileType.IMG);
                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.IMGVIEWER, path);

                if (entity instanceof MsgEntity) {
                    if (!TextUtils.isEmpty(definBean.getExt()) && ((MsgEntity) entity).getBurnstarttime() == 0 && direct == MsgDirect.From) {
                        RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.BURNMSG_READ, entity.getMsgDefinBean().getMessage_id(), direct);
                        MessageHelper.getInstance().updateMsgState(entity.getMsgid(), 2);
                    }
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
        MsgDefinBean bean = baseEntity.getMsgDefinBean();
        String url = TextUtils.isEmpty(bean.getContent()) ? bean.getUrl() : bean.getContent();
        String localPath = FileUtil.newContactFileName(baseEntity.getPubkey(), bean.getMessage_id(), FileUtil.FileType.IMG);

        if (FileUtil.islocalFile(url) || FileUtil.isExistFilePath(localPath)) {
            String local = FileUtil.islocalFile(url) ? url : localPath;
            ConversationActivity.startActivity((Activity) context, ConverType.TRANSPOND, String.valueOf(bean.getType()), local);
        }
    }
}
