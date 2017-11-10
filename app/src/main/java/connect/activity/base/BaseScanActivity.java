package connect.activity.base;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Hashtable;

import connect.ui.activity.R;
import connect.utils.BitmapUtil;
import connect.utils.ProgressUtil;
import connect.utils.ToastUtil;
import connect.utils.log.LogManager;
import connect.widget.zxing.camera.CameraManager;
import connect.widget.zxing.decode.DecodeImageCallback;
import connect.widget.zxing.decode.DecodeImageThread;
import connect.widget.zxing.utils.BeepManager;
import connect.widget.zxing.utils.CaptureActivityHandler;
import connect.widget.zxing.utils.InactivityTimer;


/**
 * Created by Administrator on 2016/12/27.
 */
public abstract class BaseScanActivity extends BaseActivity {

    private final String TAG = BaseScanActivity.class.getSimpleName();
    private CaptureActivityHandler handler;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    public boolean isHasSurface = false;

    /** SurfaceView */
    private SurfaceView scanPreview = null;
    private View scanCropView;
    private RelativeLayout scanContainer;
    private Rect mCropRect = null;
    public final int PARSE_BARCODE_SUC = 601;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        CameraManager.init();
    }

    public void setViewFind(SurfaceView scanPreview,View scanCropView,RelativeLayout scanContainer){
        this.scanPreview = scanPreview;
        this.scanCropView = scanCropView;
        this.scanContainer = scanContainer;
    }

    protected void showBackgroupAni(View view){
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
                Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
        animation.setDuration(1500);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        view.startAnimation(animation);
    }

    public abstract void scanCall(String value);

    public Handler getHandler() {
        return handler;
    }

    @Override
    protected void onStart() {
        super.onStart();
        handler = null;
        if (isHasSurface) {
            initCamera(scanPreview.getHolder());
        } else {
            scanPreview.getHolder().addCallback(callback);
        }
        inactivityTimer.onResume();
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        CameraManager.get().closeDriver();
        if (!isHasSurface) {
            scanPreview.getHolder().removeCallback(callback);
        }
        super.onPause();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (CameraManager.get().isOpen()) {
            displayFrameworkBugMessageAndExit();
            return;
        }
        try {
            CameraManager.get().openDriver(surfaceHolder);
            if (handler == null) {
                handler = new CaptureActivityHandler(this);
            }
            initCrop();
        } catch (IOException ioe) {
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            displayFrameworkBugMessageAndExit();
        }
    }

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (holder == null) {
                LogManager.getLogger().i(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
            }
            if (!isHasSurface) {
                isHasSurface = true;
                initCamera(holder);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            isHasSurface = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }
    };

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("open camera error");
        builder.setPositiveButton("sure", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }

        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        builder.show();
    }

    public void handleDecode(Result rawResult, Bundle bundle) {
        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();
        scanCall(rawResult.getText());
    }

    public void getAblamString(final String path, final Handler handler){
        ProgressUtil.getInstance().showProgress(this);
        new Thread(new DecodeImageThread(path, new DecodeImageCallback() {
            @Override
            public void decodeSucceed(Result result) {
                Message m = handler.obtainMessage();
                m.what = PARSE_BARCODE_SUC;
                m.obj = result.getText();
                handler.sendMessage(m);
            }

            @Override
            public void decodeFail(int type, String reason) {
                ProgressUtil.getInstance().dismissProgress();
                ToastUtil.getInstance().showToast("Scan failed!");
            }
        })).start();
        /*new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap scanBitmap = BitmapUtil.getInstance().compress(path, 480, 800);
                String resultString;
                if(scanBitmap != null){
                    resultString = decodeQRImage(scanBitmap);
                }else{
                    resultString = null;
                }
                if (resultString != null) {
                    Message m = handler.obtainMessage();
                    m.what = PARSE_BARCODE_SUC;
                    m.obj = resultString;
                    handler.sendMessage(m);
                } else {
                    ProgressUtil.getInstance().dismissProgress();
                    ToastUtil.getInstance().showToast("Scan failed!");
                }
            }
        }).start();*/
    }

    /**
     * Method for scanning two-dimensional code picture
     * @param bitmap
     * @return
     */
    public String decodeQRImage(Bitmap bitmap) {
        String value = null;
        Hashtable<DecodeHintType, String> hints = new Hashtable<>();
        // Encoding of two-dimensional code content
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        RGBLuminanceSource source = new RGBLuminanceSource(width,height,pixels);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader2 = new QRCodeReader();
        Result result;
        try {
            result = reader2.decode(bitmap1,hints);
            value = result.getText();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }

    /**
     * Initialize the truncated rectangle
     */
    private void initCrop() {
        int cameraWidth = CameraManager.get().getCameraResolution().height;
        int cameraHeight = CameraManager.get().getCameraResolution().width;

        /** Gets the location information of the scan frame in the layout */
        int[] location = new int[2];
        scanCropView.getLocationInWindow(location);

        int cropLeft = location[0];
        int cropTop = location[1] - getStatusBarHeight();

        int cropWidth = scanCropView.getWidth();
        int cropHeight = scanCropView.getHeight();

        /** Gets the width and height of the layout container */
        int containerWidth = scanContainer.getWidth();
        int containerHeight = scanContainer.getHeight();

        int x = cropLeft * cameraWidth / containerWidth;
        int y = cropTop * cameraHeight / containerHeight;

        int width = cropWidth * cameraWidth / containerWidth;
        int height = cropHeight * cameraHeight / containerHeight;

        mCropRect = new Rect(x, y, width + x, height + y);
    }

    private int getStatusBarHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object obj = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(obj).toString());
            return getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
