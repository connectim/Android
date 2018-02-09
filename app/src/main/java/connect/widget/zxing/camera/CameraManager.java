package connect.widget.zxing.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * This object wraps the Camera service object and expects to be the only one talking to it. The implementation
 * encapsulates the steps needed to take preview-sized images, which are used for both preview and decoding.
 */
public final class CameraManager {

    private static CameraManager sCameraManager;

    private final CameraConfigurationManager mConfigManager;
    /**
     * Preview frames are delivered here, which we pass on to the registered handler. Make sure to clear the handler so
     * it will only receive one message.
     */
    private final PreviewCallback mPreviewCallback;
    /** Auto-focus callbacks arrive here, and are dispatched to the Handler which requested them. */
    private final AutoFocusCallback mAutoFocusCallback;
    private Camera mCamera;
    private boolean mInitialized;
    private boolean mPreviewing;

    public CameraManager() {
        this.mConfigManager = new CameraConfigurationManager();
        mPreviewCallback = new PreviewCallback(mConfigManager);
        mAutoFocusCallback = new AutoFocusCallback();
    }

    /**
     * Initializes this static object with the Context of the calling Activity.
     */
    public static void init() {
        if (sCameraManager == null) {
            sCameraManager = new CameraManager();
        }
    }

    /**
     * Gets the CameraManager singleton instance.
     *
     * @return A reference to the CameraManager singleton.
     */
    public static CameraManager get() {
        return sCameraManager;
    }

    /**
     * Opens the mCamera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the mCamera will draw preview frames into.
     * @throws IOException Indicates the mCamera driver failed to open.
     */
    public boolean openDriver(SurfaceHolder holder) throws IOException {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
                if (mCamera != null) {
                    Camera.Parameters mParameters = mCamera.getParameters();
                    mCamera.setParameters(mParameters);
                    mCamera.setPreviewDisplay(holder);
                    if (!mInitialized) {
                        mInitialized = true;
                        mConfigManager.initFromCameraParameters(mCamera);
                    }
                    mConfigManager.setDesiredCameraParameters(mCamera);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Closes the camera driver if still in use.
     */
    public boolean closeDriver() {
        if (mCamera != null) {
            try {
                mCamera.release();
                mInitialized = false;
                mPreviewing = false;
                mCamera = null;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Asks the mCamera hardware to begin drawing preview frames to the screen.
     */
    public boolean startPreview() {
        if (mCamera != null && !mPreviewing) {
            try {
                mCamera.startPreview();
                mPreviewing = true;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Tells the mCamera to stop drawing preview frames.
     */
    public boolean stopPreview() {
        if (mCamera != null && mPreviewing) {
            try {
                // Removed the callback when stop the preview
                mCamera.setOneShotPreviewCallback(null);
                mCamera.stopPreview();
                mPreviewCallback.setHandler(null, 0);
                mAutoFocusCallback.setHandler(null, 0);
                mPreviewing = false;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[] in the
     * message.obj field, with width and height encoded as message.arg1 and message.arg2, respectively.
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    public void requestPreviewFrame(Handler handler, int message) {
        if (mCamera != null && mPreviewing) {
            mPreviewCallback.setHandler(handler, message);
            mCamera.setOneShotPreviewCallback(mPreviewCallback);
        }
    }

    /**
     * Asks the mCamera hardware to perform an autofocus.
     *
     * @param handler The Handler to notify when the autofocus completes.
     * @param message The message to deliver.
     */
    public void requestAutoFocus(Handler handler, int message) {
        if (mCamera != null && mPreviewing) {
            mAutoFocusCallback.setHandler(handler, message);
            // Log.d(TAG, "Requesting auto-focus callback");
            try {
                mCamera.autoFocus(mAutoFocusCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized boolean isOpen() {
        return mCamera != null;
    }

    public Camera.Size getCameraResolution() {
        return mConfigManager.getCameraResolution();
    }
}
