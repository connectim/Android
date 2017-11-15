package connect.activity.base;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import connect.utils.ProgressUtil;
import connect.utils.ToastUtil;
import connect.utils.log.LogManager;
import connect.utils.permission.PermissionUtil;
import connect.widget.zxing.camera.CameraManager;
import connect.widget.zxing.decode.DecodeImageCallback;
import connect.widget.zxing.decode.DecodeImageThread;
import connect.widget.zxing.utils.BeepManager;
import connect.widget.zxing.utils.CaptureActivityHandler;
import connect.widget.zxing.utils.InactivityTimer;

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
    public final int PARSE_BARCODE_SUC = 601;
    private BaseScanActivity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mActivity = this;

        inactivityTimer = new InactivityTimer(mActivity);
        beepManager = new BeepManager(mActivity);
        CameraManager.init();
    }

    public void setViewFind(SurfaceView scanPreview,View scanCropView,RelativeLayout scanContainer){
        this.scanPreview = scanPreview;
        this.scanCropView = scanCropView;
        this.scanContainer = scanContainer;
        PermissionUtil.getInstance().requestPermission(this, new String[]{PermissionUtil.PERMISSION_CAMERA}, permissionCallBack);
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
            Toast.makeText(mActivity, "open camera error", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            CameraManager.get().openDriver(surfaceHolder);
            if (handler == null) {
                handler = new CaptureActivityHandler(this);
            }
        } catch (IOException ioe) {
            Toast.makeText(mActivity, "open camera error", Toast.LENGTH_LONG).show();
        } catch (RuntimeException e) {
            Toast.makeText(mActivity, "open camera error", Toast.LENGTH_LONG).show();
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
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    };

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
    }

    private PermissionUtil.ResultCallBack permissionCallBack = new PermissionUtil.ResultCallBack() {
        @Override
        public void granted(String[] permissions) {
            onStart();
        }

        @Override
        public void deny(String[] permissions) {}
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.getInstance().onRequestPermissionsResult(mActivity, requestCode, permissions, grantResults, permissionCallBack);
    }

}
