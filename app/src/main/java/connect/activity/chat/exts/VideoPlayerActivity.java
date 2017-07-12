package connect.activity.chat.exts;

import android.app.Activity;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.database.green.DaoHelper.MessageHelper;
import connect.ui.activity.R;
import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.bean.RecExtBean;
import connect.activity.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.ExCountDownTimer;
import connect.utils.system.SystemDataUtil;
import connect.utils.TimeUtil;
import connect.utils.VideoPlayerUtil;
import connect.widget.TopToolBar;

/**
 * play Local video files
 */
public class VideoPlayerActivity extends BaseActivity {

    @Bind(R.id.toolbar)
    TopToolBar toolbar;
    @Bind(R.id.surfaceview)
    SurfaceView surfaceview;
    @Bind(R.id.img2)
    ImageView img2;
    @Bind(R.id.txt1)
    TextView txt1;
    @Bind(R.id.txt2)
    TextView txt2;
    @Bind(R.id.probar2)
    ProgressBar probar2;

    private VideoPlayerActivity activity;

    private Object[] objs = null;
    private String filePath;
    private int voiceLength;
    private String burnTime = "";

    private VideoPrepared videoPrepared;
    private VideoPlayFinish videoPlayFinish;
    private VideoPlayerUtil videoPlayerUtil;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window window = this.getWindow();
        window.setFlags(flag, flag);

        setContentView(R.layout.activity_videoplayer);
        ButterKnife.bind(this);
        initView();
    }

    public static void startActivity(Activity activity, Object... objects) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("Serializable", objects);
        ActivityUtil.next(activity, VideoPlayerActivity.class, bundle);
    }

    @Override
    public void initView() {
        activity = this;
        toolbar.setLeftImg(R.mipmap.back_white);
        toolbar.setLeftListence(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoPlayerUtil.stopVideo();
                ActivityUtil.goBack(activity);
            }
        });

        objs = (Object[]) getIntent().getSerializableExtra("Serializable");
        filePath = (String) objs[0];
        voiceLength = (int) objs[1];
        voiceLength *= 1000;
        if (objs.length != 2) {
            burnTime = (String) objs[2];
        }

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
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) surfaceview.getLayoutParams();
            if (width > height) {
                scale = SystemDataUtil.getScreenWidth() / width;
                params.width = SystemDataUtil.getScreenWidth();
                params.height = (int) (height * scale);
            } else {
                scale = SystemDataUtil.getScreenHeight() / height;
                params.height = SystemDataUtil.getScreenHeight();
                params.width = (int) (width * scale);
            }
            surfaceview.setLayoutParams(params);
        }


        videoPrepared = new VideoPrepared();
        videoPlayFinish = new VideoPlayFinish();
        txt2.setText(TimeUtil.getTime(voiceLength,TimeUtil.DATE_FORMAT_SECOND));
        videoPlayerUtil = VideoPlayerUtil.getInstance();
        videoPlayerUtil.init(surfaceview, videoPrepared, videoPlayFinish);
    }

    @OnClick({R.id.surfaceview, R.id.img2})
    public void OnClickListener(View view) {
        switch (view.getId()) {
            case R.id.surfaceview:
                videoPlayerUtil.stopVideo();
                ActivityUtil.goBack(activity);
                break;
            case R.id.img2:
                if (videoPlayerUtil.isPlay()) {
                    downTimer.pause();
                    videoPlayerUtil.pauseVideo();
                } else {
                    if (videoPlayerUtil.isPrePare()) {
                        downTimer.resume();
                        videoPlayerUtil.continuePlay();
                    } else {
                        playVideoFile();
                    }
                }
                break;
        }
    }

    private ExCountDownTimer downTimer;
    protected void startCountTimer() {
        if (downTimer != null) {
            downTimer.cancel();
            downTimer = null;
        }

        downTimer = new ExCountDownTimer(voiceLength, 30) {
            @Override
            public void onTick(long millisUntilFinished, int percent) {
                probar2.setProgress(percent);
                img2.setSelected(true);
                txt1.setText(TimeUtil.getTime(voiceLength - millisUntilFinished,TimeUtil.DATE_FORMAT_SECOND));
            }

            @Override
            public void onPause() {
                img2.setSelected(false);
            }

            @Override
            public void onFinish() {
                probar2.setProgress(100);
                img2.setSelected(false);
                txt1.setText(TimeUtil.getTime(voiceLength,TimeUtil.DATE_FORMAT_SECOND));
            }
        };
        downTimer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoPlayerUtil.getInstance().stopVideo();
    }

    private void playVideoFile() {
        videoPlayerUtil.playVideo(filePath);
        startCountTimer();
    }

    private class VideoPrepared implements VideoPlayerUtil.OnVideoPreparedListener {

        @Override
        public void onVideoPrepared() {
            playVideoFile();
        }
    }

    private class VideoPlayFinish implements VideoPlayerUtil.OnVideoPlayFinishListener {

        @Override
        public void onVidePlayFinish() {
            if (!TextUtils.isEmpty(burnTime)) {
                String msgid = (String) objs[3];
                MessageHelper.getInstance().updateMsgState(msgid, 2);
                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.BURNMSG_READ, msgid, MsgDirect.From);
            }
        }
    }
}
