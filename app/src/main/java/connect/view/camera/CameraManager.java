package connect.view.camera;

import android.app.Activity;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2016/12/13.
 */
public class CameraManager {

    /** If the flash open */
    private boolean isLighting = false;
    /** Whether to support flash */
    private boolean isSupportedLight = false;
    /** The camera position */
    private int cameraPosition = 0;
    private SurfaceHolder viewHolder;

    /**
     * Initialize the camera
     */
    public Camera initCamera(Activity activity,SurfaceHolder viewHolder) {
        this.viewHolder = viewHolder;
        Camera mCamera = getCameraInstance(0);
        try {
            setParametersCamera(activity,mCamera);
            List<String> features = mCamera.getParameters().getSupportedFlashModes();//Determine whether to support flash
            if (null == features || features.contains(Camera.Parameters.FLASH_MODE_ON)) {
                isLighting = false;
                isSupportedLight = true;
            }
            mCamera.setPreviewDisplay(viewHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mCamera;
    }

    public void setParametersCamera(Activity activity,Camera mCamera) {
        //mCamera.setDisplayOrientation(degrees);
        setCameraDisplayOrientation(activity,cameraPosition,mCamera);
        Camera.Parameters mParameters = mCamera.getParameters();
        int degrees = getCameraRotation(cameraPosition);
        mParameters.setRotation(degrees);

        mParameters.setJpegQuality(100);
        List<String> focusModes = mParameters.getSupportedFocusModes();
        if(focusModes.contains("continuous-video")){
            mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }
        Camera.Size previewSize = getOptimalPreviewSize(mParameters.getSupportedPreviewSizes(),
                viewHolder.getSurfaceFrame().height(),
                viewHolder.getSurfaceFrame().width());
        mParameters.setPreviewSize(previewSize.width, previewSize.height);
        mParameters.setPictureSize(previewSize.width, previewSize.height);

        mCamera.setParameters(mParameters);
    }

    /**
     * By comparison, the nearest size (if the same size is preferred) is obtained
     * Resolve the preview image
     * Source: API demo
     */
    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    /**
     * Set camera flash
     * @param mCamera
     */
    public void setFlashParameters(Camera mCamera) {
        Camera.Parameters mParameters = mCamera.getParameters();
        if (isSupportedLight) {
            String flashMode = isLighting ? Camera.Parameters.FLASH_MODE_OFF : Camera.Parameters.FLASH_MODE_TORCH;
            mParameters.setFlashMode(flashMode);
            mCamera.setParameters(mParameters);
            isLighting = !isLighting;
        }
        //setParametersCamera(mCamera);
    }

    /**
     * Set the camera to camera (turn off the old camera, turn on the new camera)
     * @param mCamera
     */
    public Camera setChangeParameters(Activity activity,Camera mCamera,SurfaceHolder viewHolder) {
        if (Camera.getNumberOfCameras() <= 1) {
            return null;
        }
        mCamera.stopPreview();
        mCamera.release();
        try {
            cameraPosition = cameraPosition == 1 ? 0 : 1;
            Camera mNewCamera = getCameraInstance(cameraPosition);
            setParametersCamera(activity,mNewCamera);
            mNewCamera.setPreviewDisplay(viewHolder);
            mNewCamera.startPreview();
            return mNewCamera;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get camera based on ID
     * @param cameraPosition
     * @return
     */
    public Camera getCameraInstance(int cameraPosition) {
        if (cameraPosition < 0 || cameraPosition > Camera.getNumberOfCameras()) {
            cameraPosition = 0;
        }
        Camera mCamera = null;
        try {
            mCamera = Camera.open(cameraPosition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mCamera;
    }

    /**
     * By comparison, the nearest size (if the same size is preferred) is obtained
     * Resolve the preview image
     * @param surfaceWidth Need to be compared to the original width
     * @param surfaceHeight Need to be compared to the original height
     * @param preSizeList Preview size list
     * @return Get the closest size to the original width height
     */
    protected Camera.Size getCloselyPreSize(int surfaceWidth, int surfaceHeight, List<Camera.Size> preSizeList) {

        int ReqTmpWidth;
        int ReqTmpHeight;
        // When the screen is perpendicular to the width of the need to replace the high value to ensure that the width is greater than the high
        ReqTmpWidth = surfaceHeight;
        ReqTmpHeight = surfaceWidth;
        /*if (mIsPortrait) {
            ReqTmpWidth = surfaceHeight;
            ReqTmpHeight = surfaceWidth;
        } else {
             ReqTmpWidth = surfaceWidth;
            ReqTmpHeight = surfaceHeight;
        }*/

        for(Camera.Size size : preSizeList){
            /*if((size.width == ReqTmpWidth) && (size.height == ReqTmpHeight)){
                return size;
            }*/
            if(size.height == ReqTmpHeight){
                return size;
            }
        }

        float reqRatio = ((float) ReqTmpWidth) / ReqTmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : preSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }
        return retSize;
    }

    protected Camera.Size getPictureSize(int surfaceWidth,int surfaceHeight, List<Camera.Size> preSizeList) {
        for(Camera.Size size : preSizeList){
            if(size.height == surfaceWidth && size.width == surfaceHeight){
                return size;
            }
        }
        return null;
    }

    public int getCameraRotation(int cameraId) {
        int result = 90;
        try {
            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(cameraId, info);
            result = info.orientation;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Adjust the camera preview
     */
    public void setCameraDisplayOrientation(Activity activity,int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public boolean isLighting() {
        return isLighting;
    }

    public void setLighting(boolean lighting) {
        isLighting = lighting;
    }

    public boolean isSupportedLight() {
        return isSupportedLight;
    }

    public int getCameraPosition() {
        return cameraPosition;
    }

    public void setCameraPosition(int cameraPosition) {
        this.cameraPosition = cameraPosition;
    }

}
