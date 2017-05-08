package connect.ui.base;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
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

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Hashtable;

import connect.ui.activity.R;
import connect.utils.BitmapUtil;
import connect.utils.PhotoUtil;
import connect.utils.ProgressUtil;
import connect.utils.ToastUtil;
import connect.utils.log.LogManager;
import connect.view.zxing.camera.CameraManager;
import connect.view.zxing.decode.DecodeThread;
import connect.view.zxing.utils.BeepManager;
import connect.view.zxing.utils.CaptureActivityHandler;
import connect.view.zxing.utils.InactivityTimer;


/**
 * Created by Administrator on 2016/12/27.
 */
public abstract class BaseScanActivity extends BaseActivity {

    private final String TAG = BaseScanActivity.class.getSimpleName();
    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    public boolean isHasSurface = false;

    /** SurfaceView */
    private SurfaceView scanPreview = null;
    private View scanCropView;
    private RelativeLayout scanContainer;
    private Rect mCropRect = null;
    private String photo_path;
    public final int PARSE_BARCODE_SUC = 601;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
    }

    public void setViewFind(SurfaceView scanPreview,View scanCropView,RelativeLayout scanContainer){
        this.scanPreview = scanPreview;
        this.scanCropView = scanCropView;
        this.scanContainer = scanContainer;
    }

    public abstract void scanCall(String value);

    public void setLineAnimation(View view){
        TranslateAnimation animation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0.9f);
        animation.setDuration(4500);
        animation.setRepeatCount(-1);
        animation.setRepeatMode(Animation.RESTART);
        view.startAnimation(animation);
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    protected void onStart() {
        super.onStart();
        cameraManager = new CameraManager(getApplication());
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
        cameraManager.closeDriver();
        if (!isHasSurface) {
            scanPreview.getHolder().removeCallback(callback);
        }
        super.onPause();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            if (handler == null) {
                handler = new CaptureActivityHandler(this, cameraManager, DecodeThread.ALL_MODE);
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
        /*bundle.putInt("width", mCropRect.width());
        bundle.putInt("height", mCropRect.height());
        bundle.putString("result", rawResult.getText());*/
        scanCall(rawResult.getText());
    }

    public void getAblamString(String path, final Handler handler){
        photo_path = path;
        ProgressUtil.getInstance().showProgress(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap scanBitmap = BitmapUtil.getSmallBitmap(photo_path,480,800);
                String resultString = decodeQRImage(scanBitmap);
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
        }).start();
    }

    public void getAblamString(Intent data, final Handler handler){
        /*Cursor cursor = getContentResolver().query(data.getData(),null,null,null,null);
        if(cursor.moveToFirst()){
            photo_path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        }
        cursor.close();*/
        Uri uri;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            String url = PhotoUtil.getPath(this,data.getData());
            uri = Uri.fromFile(new File(url));
        }else{
            uri = data.getData();
        }
        photo_path = uri.getPath();

        ProgressUtil.getInstance().showProgress(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap scanBitmap = BitmapUtil.getSmallBitmap(photo_path,480,800);
                String resultString = decodeQRImage(scanBitmap);
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
        }).start();
    }

    /**
     * Method for scanning two-dimensional code picture
     * @param bitmap
     * @return
     */
    public String decodeQRImage(Bitmap bitmap) {
        String value = null;
        Hashtable<DecodeHintType, String> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8"); // Encoding of two-dimensional code content
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
        } catch (NotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ChecksumException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return value;
    }

    /**
     * Get the rectangular area
     * @return
     */
    public Rect getCropRect() {
        return mCropRect;
    }

    /**
     * Initialize the truncated rectangle
     */
    private void initCrop() {
        int cameraWidth = cameraManager.getCameraResolution().y;
        int cameraHeight = cameraManager.getCameraResolution().x;

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
