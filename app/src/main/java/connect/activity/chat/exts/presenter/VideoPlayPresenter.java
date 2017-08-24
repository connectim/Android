package connect.activity.chat.exts.presenter;

import android.app.Activity;
import android.media.MediaMetadataRetriever;
import android.widget.RelativeLayout;

import connect.activity.chat.exts.VideoPlayerActivity;
import connect.activity.chat.exts.contract.VideoPlayContract;
import connect.utils.ExCountDownTimer;
import connect.utils.TimeUtil;
import connect.utils.VideoPlayerUtil;
import connect.utils.system.SystemDataUtil;
import connect.widget.video.inter.VideoListener;

/**
 * Created by Administrator on 2017/8/10.
 */

public class VideoPlayPresenter implements VideoPlayContract.Presenter{

    private VideoPlayContract.BView view;

    private String filePath;
    private int fileLength;
    private VideoListener videoPlayListener;
    private ExCountDownTimer countDownTimer;

    public VideoPlayPresenter(VideoPlayContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        filePath = view.getFilePath();
        fileLength = Integer.parseInt(view.getFileLength()) * 1000;
        videoPlayListener = view.getVideoListener();

        MediaMetadataRetriever retr = new MediaMetadataRetriever();
        retr.setDataSource(filePath);

        float width = 0;
        float height = 0;
        if (Float.parseFloat(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)) == 90) {
            height = Float.parseFloat(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            width = Float.parseFloat(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        } else {
            width = Float.parseFloat(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            height = Float.parseFloat(retr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
        }

        if (width != 0 && height != 0) {
            float scale = 1;
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);
            if (width > height) {
                scale = SystemDataUtil.getScreenWidth() / width;
                params.width = SystemDataUtil.getScreenWidth();
                params.height = (int) (height * scale);
            } else {
                scale = SystemDataUtil.getScreenHeight() / height;
                params.height = SystemDataUtil.getScreenHeight();
                params.width = (int) (width * scale);
            }
            view.calculateParamlayout(params);
        }

        String totallength = TimeUtil.getTime(fileLength, TimeUtil.DATE_FORMAT_SECOND);
        view.videoTotalLength(totallength);
    }

    @Override
    public void startCountDownTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        countDownTimer = new ExCountDownTimer(fileLength, 30) {
            @Override
            public void onTick(long millisUntilFinished, int percent) {
                view.playBackProgress(percent,true,TimeUtil.getTime(fileLength - millisUntilFinished,TimeUtil.DATE_FORMAT_SECOND));
            }

            @Override
            public void onPause() {

            }

            @Override
            public void onFinish() {
                view.playBackProgress(100,false,TimeUtil.getTime(fileLength,TimeUtil.DATE_FORMAT_SECOND));
            }
        };
        countDownTimer.start();
    }

    @Override
    public void pauseCountDownTimer() {
        countDownTimer.pause();
    }

    @Override
    public void resumeCountDownTimer() {
        countDownTimer.resume();
    }
}
