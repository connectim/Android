package connect.ui.activity.chat.view.holder;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Locale;

import connect.ui.activity.R;
import connect.ui.activity.chat.bean.BaseEntity;
import connect.ui.activity.chat.bean.MsgDefinBean;
import connect.ui.activity.chat.bean.MsgDirect;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.exts.VideoPlayerActivity;
import connect.ui.activity.chat.inter.FileDownLoad;
import connect.ui.activity.chat.view.BubbleImg;
import connect.ui.activity.chat.view.DVideoProView;
import connect.ui.activity.common.ConversationActivity;
import connect.ui.activity.common.bean.ConverType;
import connect.utils.FileUtil;
import connect.utils.system.SystemUtil;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgVideoHolder extends MsgChatHolder {
    private BubbleImg videomsg;
    private DVideoProView videoProView;

    private TextView timeTxt;
    private TextView sizeTxt;

    public MsgVideoHolder(View itemView) {
        super(itemView);
        videomsg = (BubbleImg) itemView.findViewById(R.id.videomsg);
        videoProView = (DVideoProView) itemView.findViewById(R.id.videoproview);
        timeTxt = (TextView) itemView.findViewById(R.id.txt1);
        sizeTxt = (TextView) itemView.findViewById(R.id.txt2);
    }

    @Override
    public void buildRowData(final MsgBaseHolder msgBaseHolder, final BaseEntity entity) {
        super.buildRowData(msgBaseHolder, entity);
        final MsgDefinBean bean = entity.getMsgDefinBean();
        String url = bean.getContent();

        long videoTime = entity.getMsgDefinBean().getSize();
        timeTxt.setText(String.format(Locale.ENGLISH,"%1$02d:%2$02d", videoTime / 60, videoTime % 60));
        sizeTxt.setText(entity.getMsgDefinBean().getExt1());

        if (!TextUtils.isEmpty(definBean.getExt())) {
            videomsg.setOpenBurn(true);
        } else if (!hasDownLoad()) {
            videomsg.setOpenBurn(true);
        } else {
            videomsg.setOpenBurn(false);
        }

        videomsg.loadUri(direct, entity.getPubkey(), bean.getMessage_id(), url);
        videomsg.setLayoutParams(calculateSize((RelativeLayout.LayoutParams) videomsg.getLayoutParams(), bean.getImageOriginWidth(), bean.getImageOriginHeight()));

        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = bean.getUrl();
                if (FileUtil.islocalFile(url)) {
                    startPlayVideo(url, bean.getSize());
                    return;
                }

                final String localPath = FileUtil.newContactFileName(entity.getPubkey(), bean.getMessage_id(), FileUtil.FileType.VIDEO);
                if (FileUtil.isExistFilePath(localPath)) {
                    String burntime = TextUtils.isEmpty(definBean.getExt()) ? "" : definBean.getExt();
                    if (TextUtils.isEmpty(burntime)) {
                        startPlayVideo(localPath, bean.getSize());
                    } else {
                        if (entity instanceof MsgEntity) {
                            if (!TextUtils.isEmpty(definBean.getExt()) && ((MsgEntity) entity).getBurnstarttime() == 0 && direct == MsgDirect.From) {
                                startPlayVideo(localPath, bean.getSize(), burntime, entity.getMsgDefinBean().getMessage_id());
                            }
                        } else {
                            startPlayVideo(localPath, bean.getSize());
                        }
                    }
                } else {
                    FileDownLoad.getInstance().downChatFile(url, entity.getPubkey(), new FileDownLoad.IFileDownLoad() {
                        @Override
                        public void successDown(byte[] bytes) {
                            videoProView.loadState(true, 0);
                            videomsg.setOpenBurn(false);
                            videomsg.loadUri(direct, entity.getPubkey(), bean.getMessage_id(), definBean.getUrl());

                            FileUtil.byteArrToFilePath(bytes, localPath);

                            String burntime = TextUtils.isEmpty(definBean.getExt()) ? "" : definBean.getExt();
                            if (TextUtils.isEmpty(burntime)) {
                                startPlayVideo(localPath, bean.getSize());
                            } else {
                                if (entity instanceof MsgEntity) {
                                    if (!TextUtils.isEmpty(definBean.getExt()) && ((MsgEntity) entity).getBurnstarttime() == 0 && direct == MsgDirect.From) {
                                        startPlayVideo(localPath, bean.getSize(), burntime, entity.getMsgDefinBean().getMessage_id());
                                    }
                                } else {
                                    startPlayVideo(localPath, bean.getSize());
                                }
                            }
                        }

                        @Override
                        public void failDown() {

                        }

                        @Override
                        public void onProgress(long bytesWritten, long totalSize) {
                            int progress = (int) (bytesWritten * 100 / totalSize);
                            videoProView.loadState(false, progress);
                        }
                    });
                }
            }
        });

        if (videoProView != null) {
            String localPath = FileUtil.newContactFileName(entity.getPubkey(), bean.getMessage_id(), FileUtil.FileType.VIDEO);
            if (FileUtil.islocalFile(url) || FileUtil.isExistFilePath(localPath)) {
                videoProView.loadState(true, 0);
            } else {
                videoProView.loadState(false, 0);
            }
        }
    }

    public void startPlayVideo(Object... objects) {
        VideoPlayerActivity.startActivity((Activity) context,objects);
    }

    public boolean hasDownLoad() {
        boolean isDown = false;
        String url = definBean.getUrl();
        if (FileUtil.islocalFile(url)) {
            isDown = true;
        } else {
            final String localPath = FileUtil.newContactFileName(baseEntity.getPubkey(), definBean.getMessage_id(), FileUtil.FileType.VIDEO);
            isDown = FileUtil.isExistFilePath(localPath);
        }
        return isDown;
    }

    private RelativeLayout.LayoutParams calculateSize(RelativeLayout.LayoutParams params, float width, float height) {
        int maxDp = SystemUtil.dipToPx(160);
        if (height != 0 && width != 0) {
            double scale = (width * 1.00) / height;
            if (width >= height) {
                width = maxDp;
                height = (int) (width / scale);
            } else {
                height = maxDp;
                width = (int) (height * scale);
            }
        } else {
            width = maxDp;
            height = maxDp;
        }
        params.width = (int) width;
        params.height = (int) height;
        return params;
    }

    @Override
    public String[] longPressPrompt() {
        return context.getResources().getStringArray(R.array.prompt_video);
    }

    @Override
    public void transPondTo() {
        super.transPondTo();
        final MsgDefinBean bean = baseEntity.getMsgDefinBean();
        String url = bean.getUrl();
        final String localPath = FileUtil.newContactFileName(baseEntity.getPubkey(), bean.getMessage_id(), FileUtil.FileType.VIDEO);

        if (FileUtil.islocalFile(url)) {
            ConversationActivity.startActivity((Activity) context, ConverType.TRANSPOND,String.valueOf(bean.getType()), url);
        } else if (FileUtil.isExistFilePath(localPath)) {
            ConversationActivity.startActivity((Activity) context, ConverType.TRANSPOND,String.valueOf(bean.getType()), localPath);
        } else {
            FileDownLoad.getInstance().downChatFile(url, baseEntity.getPubkey(), new FileDownLoad.IFileDownLoad() {
                @Override
                public void successDown(byte[] bytes) {
                    videoProView.loadState(true, 0);
                    FileUtil.byteArrToFilePath(bytes, localPath);
                    ConversationActivity.startActivity((Activity) context, ConverType.TRANSPOND, String.valueOf(bean.getType()), localPath);
                }

                @Override
                public void failDown() {

                }

                @Override
                public void onProgress(long bytesWritten, long totalSize) {
                    int progress = (int) (bytesWritten * 100 / totalSize);
                    videoProView.loadState(false, progress);
                }
            });
        }
    }
}
