package connect.widget.camera;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.IOException;

public class RecorderManager {

    private int widthSize = 1280;
    private int heightSize = 720;

    public MediaRecorder initMediaRecorder(Camera mCamera, SurfaceHolder viewHolder, File file, int frontCamera) {

        mCamera.stopPreview();
        MediaRecorder mediaRecorder = new MediaRecorder();
        mediaRecorder.reset();
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);
        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            int rotation = getCameraRotation(frontCamera);
            mediaRecorder.setOrientationHint(rotation);

            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setOutputFile(file.getPath());
            mediaRecorder.setMaxDuration(30000);
            mediaRecorder.setVideoSize(widthSize, heightSize);
            mediaRecorder.setVideoEncodingBitRate(1024 * 1024);
            mediaRecorder.setPreviewDisplay(viewHolder.getSurface());
            // prepare
            try {
                mediaRecorder.prepare();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return mediaRecorder;
        } catch (Exception e) {
            mediaRecorder = null;
            return null;
        }
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

}
