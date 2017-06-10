package connect.view.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.FileUtil;
import connect.utils.permission.PermissiomUtilNew;
import connect.utils.ToastUtil;

/**
 * Created by Administrator on 2017/2/7.
 */

public class CameraTakeActivity extends BaseActivity {

    @Bind(R.id.change_img)
    ImageView changeImg;
    @Bind(R.id.video_btn)
    VideoButtonView videoBtn;
    @Bind(R.id.close_img)
    ImageView closeImg;
    @Bind(R.id.retake_rela)
    RelativeLayout retakeRela;
    @Bind(R.id.send_rela)
    RelativeLayout sendRela;
    @Bind(R.id.camera_describe_tv)
    TextView cameraDescribeTv;
    @Bind(R.id.surfaceView_rela)
    RelativeLayout surfaceViewRela;

    private CameraTakeActivity mActivity;
    SurfaceView surfaceView;
    private CameraManager cameraManager;
    private RecorderManager recorderManager;
    private SurfaceHolder viewHolder;
    private Camera mCamera;
    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    /**
     * The media type
     */
    private File file;
    private int fileType = 0;//0：image 1：video
    public static final String MEDIA_TYPE_PHOTO = "photo";
    public static final String MEDIA_TYPE_VEDIO = "video";
    /**
     * Time management
     */
    public static final int MAX_LENGTH = 10 * 1000;
    private int videoLength = 0;
    private Runnable runnable;
    private Handler handler = new Handler();

    public static void startActivity(Activity activity, int requestCode) {
        ActivityUtil.nextBottomToTop(activity, CameraTakeActivity.class, null, requestCode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_view);
        mActivity = this;
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window window = this.getWindow();
        window.setFlags(flag, flag);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    public void initView() {
        cameraManager = new CameraManager();
        recorderManager = new RecorderManager();
        videoBtn.setEnabled(false);

        PermissiomUtilNew.getInstance().requestPermissom(mActivity, new String[]{PermissiomUtilNew.PERMISSIM_CAMERA,
                PermissiomUtilNew.PERMISSIM_RECORD_AUDIO, PermissiomUtilNew.PERMISSIM_STORAGE}, permissomCallBack);
    }

    private PermissiomUtilNew.ResultCallBack permissomCallBack = new PermissiomUtilNew.ResultCallBack() {
        @Override
        public void granted(String[] permissions) {
            surfaceView = new SurfaceView(mActivity);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            surfaceView.setLayoutParams(layoutParams);
            surfaceViewRela.removeAllViews();
            surfaceViewRela.addView(surfaceView);

            viewHolder = surfaceView.getHolder();
            viewHolder.addCallback(new SurfaceCallback());
            viewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            surfaceViewRela.setBackgroundColor(mActivity.getResources().getColor(R.color.color_clear));
            videoBtn.setEnabled(true);
            videoBtn.setOnTouchStatusListence(statusListence);
        }

        @Override
        public void deny(String[] permissions) {
        }
    };

    @OnClick(R.id.close_img)
    void goBack(View view) {
        ActivityUtil.goBackBottom(mActivity);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActivityUtil.goBackBottom(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @OnClick(R.id.change_img)
    void setChange(View view) {
        if (mCamera == null)
            return;
        Camera newCamera = cameraManager.setChangeParameters(mActivity, mCamera, viewHolder);
        if (newCamera != null) {
            mCamera = newCamera;
        }
    }

    @OnClick(R.id.retake_rela)
    void reTake(View view) {
        switchView(false);
        releasedPlay();
        FileUtil.deleteFile(file.getAbsolutePath());
        mCamera = cameraManager.initCamera(mActivity, viewHolder);
    }

    @OnClick(R.id.send_rela)
    void sendFile(View view) {
        Bundle bundle = new Bundle();
        if (fileType == 0) {
            bundle.putString("mediaType", MEDIA_TYPE_PHOTO);
        } else {
            bundle.putString("mediaType", MEDIA_TYPE_VEDIO);
            bundle.putInt("length", videoLength / 1000);
        }
        if (file != null) {
            bundle.putString("path", file.getPath());
        }
        ActivityUtil.goBackWithResult(mActivity, RESULT_OK, bundle, R.anim.activity_0_to_0, R.anim.dialog_bottom_dismiss);
        ;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissiomUtilNew.getInstance().onRequestPermissionsResult(mActivity, requestCode, permissions, grantResults, permissomCallBack);
    }

    private VideoButtonView.OnTouchStatusListence statusListence = new VideoButtonView.OnTouchStatusListence() {
        @Override
        public void clickView() {
            if (mCamera != null)
                mCamera.takePicture(null, null, mPictureCallback);
        }

        @Override
        public void longClickView() {
            if (mCamera == null)
                return;
            try {
                file = FileUtil.newTempFile(FileUtil.FileType.VIDEO);
                mediaRecorder = recorderManager.initMediaRecorder(mCamera, viewHolder, file, cameraManager.getCameraPosition());
                mediaRecorder.start();

                fileType = 1;
                changeImg.setVisibility(View.GONE);
                cameraDescribeTv.setVisibility(View.GONE);
                timing();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void cancleLongClick() {
            if (videoLength < MAX_LENGTH) {
                stopRecorder();
                initPlayVedio();
            }
        }
    };

    private void initPlayVedio() {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
        }

        mediaPlayer.reset();
        mediaPlayer = MediaPlayer.create(CameraTakeActivity.this, Uri.parse(file.getPath()));
        if (mediaPlayer == null) {
            reTake(new View(mActivity));
            ToastUtil.getInstance().showToast("Shooting video error");
            return;
        }
        switchView(true);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDisplay(viewHolder);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });
        try {
            mediaPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }

    /**
     * Photo finish callback
     */
    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            file = FileUtil.byteArrayToFile(data, FileUtil.FileType.IMG);
            fileType = 0;
            releasedCamera();
            switchView(true);
        }
    };

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mCamera == null) {
                mCamera = cameraManager.initCamera(mActivity, holder);
            } else {
                mCamera.startPreview();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            releasedCamera();
            stopRecorder();
            releasedPlay();
        }
    }

    private void switchView(boolean isFinish) {
        if (isFinish) {
            changeImg.setVisibility(View.GONE);
            TranslateAnimation transLeftAni = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                    0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
            transLeftAni.setDuration(500);
            TranslateAnimation transRightAni = new TranslateAnimation(Animation.RELATIVE_TO_SELF, -1.0f,
                    Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                    0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
            transRightAni.setDuration(500);

            retakeRela.setAnimation(transLeftAni);
            retakeRela.setVisibility(View.VISIBLE);
            sendRela.setAnimation(transRightAni);
            sendRela.setVisibility(View.VISIBLE);
            videoBtn.setVisibility(View.INVISIBLE);
            closeImg.setVisibility(View.GONE);
            cameraDescribeTv.setVisibility(View.GONE);
        } else {
            changeImg.setVisibility(View.VISIBLE);
            retakeRela.setVisibility(View.GONE);
            sendRela.setVisibility(View.GONE);
            videoBtn.setVisibility(View.VISIBLE);
            closeImg.setVisibility(View.VISIBLE);
            cameraDescribeTv.setVisibility(View.VISIBLE);
        }
    }

    private void timing() {
        videoLength = 0;
        runnable = new Runnable() {
            @Override
            public void run() {
                videoLength += 50;
                if (videoLength > MAX_LENGTH) {
                    stopRecorder();
                    initPlayVedio();
                } else {
                    videoBtn.setPeogressCricle(Float.valueOf(videoLength) / 1000);
                    handler.postDelayed(this, 50);
                }
            }
        };
        handler.postDelayed(runnable, 50);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasedCamera();
        stopRecorder();
        releasedPlay();
    }

    private void releasedCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private void stopRecorder() {
        pauseAudioRecord();
        try {
            if (mediaRecorder != null) {
                //The following three parameters must be added, if not, it will collapse, in mediarecorder.stop ();
                //Exception：RuntimeException:stop failed
                mediaRecorder.setOnErrorListener(null);
                mediaRecorder.setOnInfoListener(null);
                mediaRecorder.setPreviewDisplay(null);
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        releasedCamera();
    }

    /**
     * Stop time and progress bar
     */
    public void pauseAudioRecord() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
            runnable = null;
        }
    }

    /**
     * Release the player
     */
    private void releasedPlay() {
        pauseAudioRecord();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

}
