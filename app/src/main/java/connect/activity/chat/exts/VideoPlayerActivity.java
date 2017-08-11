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

import java.io.Serializable;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.activity.chat.exts.contract.VideoPlayContract;
import connect.activity.chat.exts.presenter.VideoPlayPresenter;
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
public class VideoPlayerActivity extends BaseActivity implements VideoPlayContract.BView{

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
    private int voiceLength;
    private String burnTime = "";
    private VideoPlayerUtil videoPlayerUtil;

    private VideoPlayContract.Presenter presenter;
    private String filePath;
    private String length;
    private VideoPlayerUtil.VideoPlayListener callBackListener;

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

    public static void startActivity(Activity activity, String filepath, String length, VideoPlayerUtil.VideoPlayListener listener) {
        Bundle bundle = new Bundle();
        bundle.putString("PATH", filepath);
        bundle.putString("LENGTH", length);
        bundle.putSerializable("LISTENER", listener);
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

        filePath = getIntent().getStringExtra("PATH");
        length = getIntent().getStringExtra("LENGTH");
        callBackListener = (VideoPlayerUtil.VideoPlayListener) getIntent().getSerializableExtra("LISTENER");

        videoPlayerUtil = VideoPlayerUtil.getInstance();
        videoPlayerUtil.init(surfaceview, new VideoPlayerUtil.VideoPlayListener() {
            @Override
            public void onVideoPrepared() {
                playVideoFile();
            }

            @Override
            public void onVidePlayFinish() {
                callBackListener.onVidePlayFinish();
            }
        });

        new VideoPlayPresenter(this).start();
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
                    presenter.pauseCountDownTimer();
                    videoPlayerUtil.pauseVideo();
                } else {
                    if (videoPlayerUtil.isPrePare()) {
                        presenter.resumeCountDownTimer();
                        videoPlayerUtil.continuePlay();
                    } else {
                        playVideoFile();
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        VideoPlayerUtil.getInstance().stopVideo();
    }

    private void playVideoFile() {
        videoPlayerUtil.playVideo(filePath);
        presenter.startCountDownTimer();
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public String getFileLength() {
        return length;
    }

    @Override
    public void setPresenter(VideoPlayContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Activity getActivity() {
        return activity;
    }

    @Override
    public VideoPlayerUtil.VideoPlayListener getVideoListener() {
        return callBackListener;
    }

    @Override
    public void calculateParamlayout(RelativeLayout.LayoutParams params) {
        surfaceview.setLayoutParams(params);
    }

    @Override
    public void videoTotalLength(String lengthformat) {
        txt2.setText(lengthformat);
    }

    @Override
    public void playBackProgress(int percent, boolean select, String lengthformat) {
        probar2.setProgress(percent);
        img2.setSelected(select);
        txt1.setText(lengthformat);
    }
}
