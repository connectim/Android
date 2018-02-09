package connect.utils;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.io.Serializable;

/**
 * video player tool
 */
public class VideoPlayerUtil implements OnPreparedListener,
        OnBufferingUpdateListener, OnCompletionListener, SurfaceHolder.Callback {

    private static VideoPlayerUtil mVideoUtil;

    private MediaPlayer mediaPlayer;
    private SurfaceHolder surfaceHolder;
    private int mVideoWidth;
    private int mVideoHeight;

    private VideoPlayListener videoPlayListener;

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceHolder = holder;
        videoPlayListener.onVideoPrepared();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public synchronized static VideoPlayerUtil getInstance() {
        if (null == mVideoUtil) {
            mVideoUtil = new VideoPlayerUtil();
        }
        return mVideoUtil;
    }

    public void init(SurfaceView sf, VideoPlayListener listener) {
        this.videoPlayListener = listener;

        surfaceHolder = sf.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public boolean playVideo(String filepath) {
        if (mediaPlayer != null) {
            freeMediaPlayerResource();
        }

        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(filepath);
            mediaPlayer.setDisplay(surfaceHolder);
            mediaPlayer.prepareAsync();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return false;
        } catch (IllegalStateException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        return true;
    }

    public boolean isPrePare() {
        return mediaPlayer != null;
    }

    public boolean isPlay() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public void continuePlay() {
        if (mediaPlayer != null)
            mediaPlayer.start();
    }

    public void pauseVideo() {
        if (mediaPlayer != null)
            mediaPlayer.pause();
    }

    public void stopVideo() {
        if (mediaPlayer != null)
            mediaPlayer.stop();
        freeMediaPlayerResource();
    }

    @Override
    public void onCompletion(MediaPlayer arg0) {
        videoPlayListener.onVidePlayFinish();
        freeMediaPlayerResource();
    }

    /**
     * release mediaplayer
     */
    private void freeMediaPlayerResource() {
        if (mediaPlayer != null) {
            mediaPlayer.setOnBufferingUpdateListener(null);
            mediaPlayer.setOnPreparedListener(null);
            mediaPlayer.setOnCompletionListener(null);

            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
        mVideoWidth = mediaPlayer.getVideoWidth();
        mVideoHeight = mediaPlayer.getVideoHeight();
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            surfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
            mediaPlayer.start();
        }
    }

    @Override
    public void onPrepared(MediaPlayer arg0) {
        mVideoWidth = mediaPlayer.getVideoWidth();
        mVideoHeight = mediaPlayer.getVideoHeight();
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            surfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);
            mediaPlayer.start();
        }
    }

    public interface VideoPlayListener extends Serializable {

        void onVideoPrepared();

        void onVidePlayFinish();
    }
}
