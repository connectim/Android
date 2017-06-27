package connect.ui.activity.chat.view.holder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.io.File;

import connect.db.green.DaoHelper.MessageHelper;
import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.bean.MsgDirect;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.bean.RecExtBean;
import connect.ui.activity.chat.view.BubbleImg;
import connect.ui.activity.common.selefriend.ConversationActivity;
import connect.ui.activity.common.bean.ConverType;
import connect.ui.base.BaseApplication;
import connect.utils.FileUtil;
import connect.utils.system.SystemUtil;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgImgHolder extends MsgChatHolder {
    private BubbleImg imgmsg;

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
        imgmsg.loadUri(direct, entity.getPubkey(), bean.getMessage_id(), url,definBean.getImageOriginWidth(),definBean.getImageOriginHeight());

        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MsgDefinBean bean = entity.getMsgDefinBean();
                String thumb = bean.getContent();
                String path = FileUtil.islocalFile(thumb) ? thumb : FileUtil.newContactFileName(entity.getPubkey(), bean.getMessage_id(), FileUtil.FileType.IMG);
                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.IMGVIEWER, path);

                if (entity instanceof MsgEntity) {
                    if (!TextUtils.isEmpty(definBean.getExt()) && ((MsgEntity) entity).getBurnstarttime() == 0 && direct == MsgDirect.From) {
                        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.BURNMSG_READ, entity.getMsgDefinBean().getMessage_id(), direct);
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
        final MsgDefinBean bean = baseEntity.getMsgDefinBean();
        String local = TextUtils.isEmpty(bean.getContent()) ? bean.getUrl() : bean.getContent();
        Glide.with(BaseApplication.getInstance())
                .load(local)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        File file = FileUtil.createAbsNewFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/connect/" + bean.getMessage_id() + FileUtil.FileType.IMG.getFileType());
                        MediaStore.Images.Media.insertImage(context.getContentResolver(), resource, "connect", "");
                        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
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
