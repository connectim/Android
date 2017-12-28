package connect.activity.chat.view.holder;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import connect.activity.chat.bean.RoomSession;
import connect.activity.chat.exts.VideoPlayerActivity;
import connect.activity.chat.view.BubbleImg;
import connect.activity.chat.view.DVideoProView;
import connect.ui.activity.R;
import connect.utils.FileUtil;
import connect.utils.chatfile.download.DownLoadFile;
import connect.utils.chatfile.inter.InterFileDown;
import connect.widget.selefriend.SelectRecentlyChatActivity;
import instant.bean.ChatMsgEntity;
import protos.Connect;

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
    public void buildRowData(final MsgBaseHolder msgBaseHolder, final ChatMsgEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        final Connect.VideoMessage videoMessage = Connect.VideoMessage.parseFrom(msgExtEntity.getContents());
        RoomSession.getInstance().checkBurnTime(videoMessage.getSnapTime());

        int videoTime = videoMessage.getTimeLength();
        timeTxt.setText(String.format(Locale.ENGLISH, "%1$02d:%2$02d", videoTime / 60, videoTime % 60));
        sizeTxt.setText(FileUtil.fileSize(videoMessage.getSize()));

        if (videoMessage.getSnapTime() == 0) {
            videomsg.setOpenBurn(false);
        } else if (!hasDownLoad()) {
            videomsg.setOpenBurn(true);
        } else {
            videomsg.setOpenBurn(true);
        }

        final Connect.ChatType chatType = Connect.ChatType.forNumber(msgExtEntity.getChatType());
        videomsg.loadUri(msgExtEntity.parseDirect(), chatType, msgExtEntity.getMessage_ower(), msgExtEntity.getMessage_id(),
                "", videoMessage.getCover(), videoMessage.getImageWidth(), videoMessage.getImageHeight());
        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = videoMessage.getUrl();
                if (FileUtil.isLocalFile(url)) {
                    startPlayVideo(url, videoMessage.getTimeLength(), "");
                } else {
                    final String localPath = FileUtil.newContactFileName(msgExtEntity.getMessage_ower(),
                            msgExtEntity.getMessage_id(), FileUtil.FileType.VIDEO);
                    if (FileUtil.isExistFilePath(localPath)) {
                        if (videoMessage.getSnapTime() == 0) {
                            startPlayVideo(localPath, videoMessage.getTimeLength(), "");
                        } else {
                            startPlayVideo(localPath, videoMessage.getTimeLength(), msgExtEntity.getMessage_id());
                        }
                    } else {
                        DownLoadFile loadFile = new DownLoadFile(url, new InterFileDown() {
                            @Override
                            public void successDown(byte[] bytes) {
                                videoProView.loadState(true, 0);
                                videomsg.setOpenBurn(false);
                                videomsg.loadUri(msgExtEntity.parseDirect(), chatType, msgExtEntity.getMessage_ower(), msgExtEntity.getMessage_id(),
                                        "", videoMessage.getUrl(), videoMessage.getImageWidth(), videoMessage.getImageHeight());

                                FileUtil.byteArrToFilePath(bytes, localPath);
                                if (videoMessage.getSnapTime() == 0) {
                                    startPlayVideo(localPath, videoMessage.getTimeLength(), "");
                                } else {
                                    startPlayVideo(localPath, videoMessage.getTimeLength(), msgExtEntity.getMessage_id());
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
                        loadFile.downFile();
                    }
                }
            }
        });

        if (videoProView != null) {
            String localPath = FileUtil.newContactFileName(msgExtEntity.getMessage_ower(), msgExtEntity.getMessage_id(), FileUtil.FileType.VIDEO);
            if (FileUtil.isLocalFile(videoMessage.getUrl()) || FileUtil.isExistFilePath(localPath)) {
                videoProView.loadState(true, 0);
            } else {
                videoProView.loadState(false, 0);
            }
        }
    }

    public void startPlayVideo(String filepath, int videolength, final String messageid) {
        VideoPlayerActivity.startActivity((Activity) context, filepath, String.valueOf(videolength), messageid);
    }

    public boolean hasDownLoad() {
        ChatMsgEntity msgExtEntity = getMsgExtEntity();
        boolean isDown = false;
        try {
            Connect.VideoMessage videoMessage = Connect.VideoMessage.parseFrom(msgExtEntity.getContents());
            String url = videoMessage.getUrl();
            if (FileUtil.isLocalFile(url)) {
                isDown = true;
            } else {
                final String localPath = FileUtil.newContactFileName(msgExtEntity.getMessage_ower(), msgExtEntity.getMessage_id(), FileUtil.FileType.VIDEO);
                isDown = FileUtil.isExistFilePath(localPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isDown;
    }

    @Override
    public String[] longPressPrompt() {
        return context.getResources().getStringArray(R.array.prompt_video);
    }

    @Override
    public void transPondTo() {
        super.transPondTo();
        final ChatMsgEntity msgExtEntity = getMsgExtEntity();
        try {
            final Connect.VideoMessage videoMessage = Connect.VideoMessage.parseFrom(msgExtEntity.getContents());
            String url = videoMessage.getUrl();
            final String localPath = FileUtil.newContactFileName(msgExtEntity.getMessage_ower(), msgExtEntity.getMessage_id(), FileUtil.FileType.VIDEO);

            if (FileUtil.isLocalFile(url)) {
                SelectRecentlyChatActivity.startActivity((Activity) context, SelectRecentlyChatActivity.TRANSPOND, String.valueOf(msgExtEntity.getMessageType()), url, videoMessage.getTimeLength());
            } else if (FileUtil.isExistFilePath(localPath)) {
                SelectRecentlyChatActivity.startActivity((Activity) context, SelectRecentlyChatActivity.TRANSPOND, String.valueOf(msgExtEntity.getMessageType()), localPath, videoMessage.getTimeLength());
            } else {
                DownLoadFile loadFile = new DownLoadFile(url, new InterFileDown() {
                    @Override
                    public void successDown(byte[] bytes) {
                        videoProView.loadState(true, 0);
                        FileUtil.byteArrToFilePath(bytes, localPath);
                        SelectRecentlyChatActivity.startActivity((Activity) context, SelectRecentlyChatActivity.TRANSPOND, String.valueOf(msgExtEntity.getMessageType()), localPath, videoMessage.getTimeLength());
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
                loadFile.downFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
