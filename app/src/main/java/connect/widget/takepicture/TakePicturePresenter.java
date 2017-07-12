package connect.widget.takepicture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.File;

import connect.utils.BitmapUtil;
import connect.utils.FileUtil;
import connect.utils.permission.PermissionUtil;
import connect.widget.camera.CameraManager;

/**
 * Created by Administrator on 2017/6/29 0029.
 */

public class TakePicturePresenter implements TakePictureContract.Presenter{

    private TakePictureContract.View mView;
    private CameraManager cameraManager;
    private Camera mCamera;
    private boolean safeToTakePicture = true;

    public TakePicturePresenter(TakePictureContract.View mView) {
        this.mView = mView;
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        cameraManager = new CameraManager();
        PermissionUtil.getInstance().requestPermissom(mView.getActivity(), new String[]{PermissionUtil.PERMISSIM_CAMERA,
                PermissionUtil.PERMISSIM_STORAGE}, permissionCallBack);
    }

    private PermissionUtil.ResultCallBack permissionCallBack = new PermissionUtil.ResultCallBack() {
        @Override
        public void granted(String[] permissions) {
            mView.initCameraView();
        }

        @Override
        public void deny(String[] permissions) {

        }
    };

    public class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (mCamera == null) {
                mCamera = cameraManager.initCamera(mView.getActivity(),holder);
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
    public PermissionUtil.ResultCallBack getPermissionCallBack() {
        return permissionCallBack;
    }

    @Override
    public SurfaceHolder.Callback getSurfaceCallback() {
        return new SurfaceCallback();
    }

    @Override
    public void setTakePhoto(Camera.PictureCallback mPictureCallback) {
        if (mCamera == null)
            return;
        try {
            if(safeToTakePicture){
                mCamera.takePicture(null, null, mPictureCallback);
                safeToTakePicture = false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void setChangeCamera(SurfaceHolder viewHolder) {
        if (mCamera != null) {
            Camera newCamera = cameraManager.setChangeParameters(mView.getActivity(),mCamera, viewHolder);
            if (newCamera != null) {
                mCamera = newCamera;
            }
        }
    }

    @Override
    public String getPicturePath(byte[] data, int retY) {
        File file = FileUtil.byteArrayToFile(data, FileUtil.FileType.IMG);
        releasedCamera();
        if (file == null) {
            //no path to picture, return
            safeToTakePicture = true;
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
        int w = bitmap.getWidth();
        int h = w;
        if (retY + w > bitmap.getHeight()) {
            h = bitmap.getHeight();
            retY = 0;
        }
        Bitmap cropBitmap = Bitmap.createBitmap(bitmap, 0, retY, w, h, null, false);
        File fileCrop = BitmapUtil.getInstance().bitmapSavePath(cropBitmap);
        FileUtil.deleteFile(file.getPath());
        safeToTakePicture = true;
        return fileCrop.getAbsolutePath();
    }

    public void releasedCamera() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}

