package connect.activity.chat.view.holder;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import connect.activity.chat.bean.DestructReadBean;
import connect.utils.chatfile.download.DownLoadFile;
import connect.utils.chatfile.inter.InterFileDown;
import instant.bean.ChatMsgEntity;
import instant.bean.MsgDirect;
import connect.activity.chat.view.VoiceImg;
import connect.database.green.DaoHelper.MessageHelper;
import connect.ui.activity.R;
import connect.utils.FileUtil;
import connect.utils.TimeUtil;
import protos.Connect;

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
    public void buildRowData(final MsgBaseHolder msgBaseHolder, final ChatMsgEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        final Connect.VoiceMessage voiceMessage = Connect.VoiceMessage.parseFrom(msgExtEntity.getContents());

        boolean visiable = false;
        if (msgExtEntity.parseDirect() == MsgDirect.From) {
            Long readTime = msgExtEntity.getRead_time();
            if (readTime == null) {
                visiable = true;
            } else if (readTime != null) {
                visiable = 0 == msgExtEntity.getRead_time();
            }
            view1.setVisibility(visiable ? View.VISIBLE : View.GONE);
        }

        voiceImg.loadVoice(msgExtEntity.parseDirect(), voiceMessage.getTimeLength());
        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = voiceMessage.getUrl();
                msgExtEntity.setRead_time(TimeUtil.getCurrentTimeInLong());
                MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);

                if (FileUtil.isLocalFile(url) || FileUtil.isExistFilePath(url)) {
                    voiceImg.startPlay(url);
                } else {
                    msgExtEntity.setRead_time(TimeUtil.getCurrentTimeInLong());
                    if (view1 != null) {
                        view1.setVisibility(View.GONE);
                    }

                    final String localPath = FileUtil.newContactFileName(msgExtEntity.getMessage_ower(), msgExtEntity.getMessage_id(), FileUtil.FileType.VOICE);
                    if (FileUtil.isExistFilePath(localPath)) {
                        voiceImg.startPlay(localPath);
                    } else {
                        voiceImg.downLoading();

                        Connect.ChatType chatType = Connect.ChatType.forNumber(msgExtEntity.getChatType());
                        DownLoadFile loadFile = new DownLoadFile(chatType, msgExtEntity.getMessage_ower(), msgExtEntity.getEcdh(),url, new InterFileDown() {
                            @Override
                            public void successDown(byte[] bytes) {
                                if (null != loadImg) {
                                    loadImg.setVisibility(View.GONE);
                                }

                                FileUtil.byteArrToFilePath(bytes, localPath);
                                voiceImg.startPlay(msgExtEntity.getMessage_id(), localPath, new VoiceImg.VoicePlayListener() {
                                    @Override
                                    public void playFinish(String msgid, String filepath) {
                                        if (msgExtEntity.getSnap_time() == 0 && msgExtEntity.parseDirect() == MsgDirect.From) {
                                            msgExtEntity.setSnap_time(TimeUtil.getCurrentTimeInLong());
                                            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);

                                            DestructReadBean.getInstance().sendEventDelay(msgExtEntity.getMessage_id());
                                        }
                                    }
                                });
                            }

                            @Override
                            public void failDown() {

                            }

                            @Override
                            public void onProgress(long bytesWritten, long totalSize) {
                                int progress = (int) (bytesWritten * 100 / totalSize);
                                if (null != loadImg) {
                                    loadImg.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                        loadFile.downFile();
                    }
                }
            }
        });
    }
}
