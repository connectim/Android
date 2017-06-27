package connect.activity.chat.view.holder;

import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.MessageEntity;
import connect.ui.activity.R;
import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.inter.FileDownLoad;
import connect.activity.chat.view.VoiceImg;
import connect.utils.FileUtil;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgVoiceHolder extends MsgChatHolder {
    private View view1;
    private VoiceImg voiceImg;
    private ImageView loadImg;

    public MsgVoiceHolder(View itemView) {
        super(itemView);
        view1 = itemView.findViewById(R.id.view1);
        voiceImg = (VoiceImg) itemView.findViewById(R.id.voicemsg);
        loadImg = (ImageView) itemView.findViewById(R.id.img1);

        if (loadImg != null) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.loading_white);
            loadImg.startAnimation(animation);
        }
    }

    @Override
    public void buildRowData(final MsgBaseHolder msgBaseHolder, final MsgEntity entity) {
        super.buildRowData(msgBaseHolder, entity);
        MsgDefinBean bean = entity.getMsgDefinBean();

        if (direct == MsgDirect.From && view1 != null) {
            if (entity.getReadstate() == 0) {
                view1.setVisibility(View.VISIBLE);
            } else {
                view1.setVisibility(View.GONE);
            }
        }

        voiceImg.loadVoice(direct, bean.getSize());
        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MsgDefinBean bean = entity.getMsgDefinBean();
                String url = bean.getContent();

                MessageHelper.getInstance().updateMsgState(bean.getMessage_id(), 1);

                if (FileUtil.islocalFile(url) || FileUtil.isExistFilePath(url)) {
                    voiceImg.startPlay(url);
                } else {
                    entity.setReadstate(1);
                    if (view1 != null) {
                        view1.setVisibility(View.GONE);
                    }
                    MessageEntity msgEntity = MessageHelper.getInstance().loadMsgByMsgid(bean.getMessage_id());
                    if (msgEntity != null) {
                        msgEntity.setState(1);
                        MessageHelper.getInstance().updateMsg(msgEntity);
                    }

                    final String localPath = FileUtil.newContactFileName(entity.getPubkey(), bean.getMessage_id(), FileUtil.FileType.VOICE);
                    if (FileUtil.isExistFilePath(localPath)) {
                        voiceImg.startPlay(localPath);

                        if (entity instanceof MsgEntity) {
                            if (!TextUtils.isEmpty(definBean.getExt()) && ((MsgEntity) entity).getBurnstarttime() == 0 && direct == MsgDirect.From) {
                                voiceImg.setPlayListener(new VoiceImg.VoicePlayListener() {
                                    @Override
                                    public void playFinish(String msgid, String filepath) {
                                        MessageHelper.getInstance().updateMsgState(entity.getMsgid(), 2);
                                        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.BURNMSG_READ, msgid, direct);
                                    }
                                });
                            }
                        }
                    } else {
                        voiceImg.downLoading();
                        FileDownLoad.getInstance().downChatFile(url, entity.getPubkey(), new FileDownLoad.IFileDownLoad() {
                            @Override
                            public void successDown(byte[] bytes) {
                                loadImg.setVisibility(View.GONE);

                                FileUtil.byteArrToFilePath(bytes, localPath);
                                voiceImg.startPlay(entity.getMsgDefinBean().getMessage_id(), localPath);

                                if (entity instanceof MsgEntity) {
                                    if (!TextUtils.isEmpty(definBean.getExt1()) && ((MsgEntity) entity).getBurnstarttime() == 0 && direct == MsgDirect.From) {
                                        voiceImg.setPlayListener(new VoiceImg.VoicePlayListener() {
                                            @Override
                                            public void playFinish(String msgid, String filepath) {
                                                MessageHelper.getInstance().updateMsgState(entity.getMsgid(), 2);
                                                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.BURNMSG_READ, msgid, direct);
                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void failDown() {

                            }

                            @Override
                            public void onProgress(long bytesWritten, long totalSize) {
                                int progress = (int) (bytesWritten * 100 / totalSize);
                                loadImg.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            }
        });
    }
}
