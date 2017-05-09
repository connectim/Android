package connect.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import connect.ui.activity.chat.bean.RecExtBean;
import connect.ui.base.BaseApplication;

/**
 * media play
 * Created by gtq on 2016/11/28.
 */
public class MediaUtil {

    private static MediaUtil mediaUtil;

    public static MediaUtil getInstance() {
        if (mediaUtil == null) {
            mediaUtil = new MediaUtil();
        }
        return mediaUtil;
    }

    private MediaPlayer mediaPlayer;

    private String filePath = null;

    public synchronized void playVoice(String path) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    mediaPlayer.reset();
                    return false;
                }
            });
        } else {
            mediaPlayer.reset();
            if (!filePath.equals(path)) {//Close the last playback state
                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.VOICE_RELEASE, filePath);
            }
        }

        this.filePath = path;
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {//Play to the full
            @Override
            public void onCompletion(MediaPlayer mp) {
                RecExtBean.sendRecExtMsg(RecExtBean.ExtType.VOICE_COMPLETE, filePath);
            }
        });
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        } catch (Exception e) {
            // TODO: handle exception
        }
        mediaPlayer.start();
    }

    /**
     * release MediaPlayer
     */
    public void freeMediaPlayerResource() {
        if (null != mediaPlayer) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        //stop play voice
        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.VOICE_RELEASE, filePath);
    }

    public boolean isPlayVoive() {
        AudioManager mAudioManager = (AudioManager) BaseApplication.getInstance().getBaseContext().
                getSystemService(Context.AUDIO_SERVICE);
        return mAudioManager.isMusicActive();
    }

    public String getFilePath() {
        return filePath;
    }
}