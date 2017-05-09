package connect.ui.activity.login;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import connect.ui.activity.R;
import connect.ui.base.BaseActivity;
import connect.utils.ActivityUtil;
import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import connect.utils.permission.PermissiomUtilNew;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;
import connect.view.TopToolBar;
import connect.view.album.entity.ImageInfo;
import connect.view.album.ui.activity.PhotoAlbumActivity;
import connect.view.camera.CameraManager;
import connect.view.camera.FileSavaManage;
import connect.view.clip.ClipImageActivity;

/**
 * Registration to take photo
 * Created by john on 2016/11/23.
 */

public class RegisterPhotoActivity extends BaseActivity {

    @Bind(R.id.toolbar_top)
    TopToolBar toolbarTop;
    @Bind(R.id.surface_rela)
    RelativeLayout surfaceRela;
    @Bind(R.id.sele_photos_tv)
    ImageView selePhotosTv;
    @Bind(R.id.takePhoto_img)
    RelativeLayout takePhotoImg;
    @Bind(R.id.switch_photos_img)
    ImageView switchPhotosImg;
    @Bind(R.id.surfaceView_rela)
    RelativeLayout surfaceViewRela;

    private RegisterPhotoActivity mActivity;
    SurfaceView surfaceView;
    private SurfaceHolder viewHolder;
    private Camera mCamera;
    private CameraManager cameraManager;
    private FileSavaManage fileSavaManage;
    private File file;
    private String photo_path = "";
    public static final int REQUEST_CODE = 100;

    public static void startActivity(Activity activity) {
        ActivityUtil.next(activity, RegisterPhotoActivity.class, REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_phono);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initView();
    }

    @Override
    public void initView() {
        mActivity = this;
        toolbarTop.setBlackStyle();
        toolbarTop.setLeftImg(R.mipmap.back_white);
        toolbarTop.setTitle(null, R.string.Login_Take_Photo);

        fileSavaManage = new FileSavaManage();
        cameraManager = new CameraManager();
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(SystemDataUtil.getScreenWidth(), SystemDataUtil.getScreenWidth());
        layoutParams.addRule(RelativeLayout.BELOW, R.id.toolbar_top);
        surfaceRela.setLayoutParams(layoutParams);

        PermissiomUtilNew.getInstance().requestPermissom(mActivity, new String[]{PermissiomUtilNew.PERMISSIM_CAMERA,
                PermissiomUtilNew.PERMISSIM_STORAGE}, permissomCallBack);
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
        }

        @Override
        public void deny(String[] permissions) {

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissiomUtilNew.getInstance().onRequestPermissionsResult(mActivity, requestCode, permissions, grantResults, permissomCallBack);
    }

    @OnClick(R.id.left_img)
    void goBack(View view) {
        ActivityUtil.goBack(mActivity);
    }

    @OnClick(R.id.takePhoto_img)
    void setTakePhoto(View view) {
        takePhotoImg.setEnabled(false);
        if (mCamera == null)
            return;
        mCamera.takePicture(null, null, mPictureCallback);
    }

    @OnClick(R.id.sele_photos_tv)
    void setSelePhoto(View view) {
        PhotoAlbumActivity.startActivity(mActivity, PhotoAlbumActivity.OPEN_ALBUM_CODE, 1);
    }

    @OnClick(R.id.switch_photos_img)
    void setChange(View view) {
        if (mCamera != null) {
            Camera newCamera = cameraManager.setChangeParameters(mActivity,mCamera, viewHolder);
            if (newCamera != null) {
                mCamera = newCamera;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == PhotoAlbumActivity.OPEN_ALBUM_CODE && requestCode == PhotoAlbumActivity.OPEN_ALBUM_CODE) {
            List<ImageInfo> strings = (List<ImageInfo>) data.getSerializableExtra("list");
            if (strings != null && strings.size() > 0) {
                ClipImageActivity.startActivity(mActivity, strings.get(0).getImageFile().getAbsolutePath(), ClipImageActivity.REQUEST_CODE);
            }
        }

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ClipImageActivity.REQUEST_CODE:
                    photo_path = data.getExtras().getString("path");
                    PreviewPhotoActivity.startActivity(mActivity, photo_path);
                    break;
                default:
                    break;
            }
        }
    }

    private void releasedCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    private Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            takePhotoImg.setEnabled(true);
            file = fileSavaManage.getPhotoFile(data);
            releasedCamera();
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            /*Matrix matrix = new Matrix();
            matrix.postRotate(cameraManager.getCameraDisplayOrientation(mActivity, cameraManager.getCameraPosition()));
            matrix.postScale(1, cameraManager.getCameraPosition()==1 ? -1 : 1);
            Bitmap cropRotateScaled = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);*/

            int w = bitmap.getWidth();
            int retY = toolbarTop.getHeight();
            int h = w;
            if (retY + w > bitmap.getHeight()) {
                h = bitmap.getHeight();
                retY = 0;
            }
            Bitmap cropBitmap = Bitmap.createBitmap(bitmap, 0, retY, w, h, null, false);
            String cropPath = BitmapUtil.bitmapSavePath(cropBitmap, null,100);

            FileUtil.deleteFile(file.getPath());
            PreviewPhotoActivity.startActivity(mActivity, cropPath);
        }
    };

    private class SurfaceCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mCamera == null) {
                mCamera = cameraManager.initCamera(mActivity,holder);
            } else {
                mCamera.startPreview();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            releasedCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasedCamera();
    }

}
