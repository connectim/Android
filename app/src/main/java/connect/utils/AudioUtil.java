package connect.utils;

import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by gtq on 2016/11/28.
 */
public class AudioUtil {
    private static AudioUtil audioUtil;

    /** Total recording time */
    private final long RECORD_TOTAL = 60000;
    /** Recording timing frequency */
    private final long RECORD_FREQUENCY = 300;
    /** Recording timer recording up to 60s per 1s return recording decibel */
    private RecordTimer recordTimer = new RecordTimer(RECORD_TOTAL, RECORD_FREQUENCY);
    /** Recording address */
    private String recordPath=null;
    private MediaRecorder mediaRecorder;

    public static AudioUtil getInstance() {
        if (audioUtil == null) {
            audioUtil = new AudioUtil();
        }
        return audioUtil;
    }

    /**
     * record voice
     */
    public void prepareAudio() {
        File dir = FileUtil.newContactFile(FileUtil.FileType.VOICE);
        if (dir == null) {
            return;
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }

        recordPath = generateAudioName();
        mediaRecorder = new MediaRecorder();

        try {
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFile(recordPath);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (Exception e) {
            //When you enter the chat interface, Home key to exit the voice off
            e.printStackTrace();
            recordListener.startError();
            mediaRecorder = null;
            return;
        }

        recordTimer.start();
        if (recordListener != null) {
            recordListener.wellPrepared();
        }
    }

    /**
     * Release audio resources
     */
    private void releaseRecorder() {
        if (mediaRecorder != null) {
            //// TODO: 2017/3/23   The following three parameters must be added, if not, it will collapse，mediarecorder.stop();
           //// TODO: 2017/3/23     RuntimeException:stop failed
            mediaRecorder.setOnErrorListener(null);
            mediaRecorder.setOnInfoListener(null);
            mediaRecorder.setPreviewDisplay(null);

            try {
                mediaRecorder.stop();
                mediaRecorder.release();
                mediaRecorder = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void cancleRecord() {
        releaseRecorder();
        if (!TextUtils.isEmpty(recordPath)) {
            File file = new File(recordPath);
            file.delete();
            recordPath = null;
        }
    }

    /**
     * Record complete
     */
    public void finishRecorder() {
        recordTimer.cancel();
        releaseRecorder();
        if (recordListener != null && !TextUtils.isEmpty(recordPath)) {
            recordListener.recordFinish(recordPath);
            recordPath = null;
        }
    }

    public AudioRecordListener recordListener;

    public interface AudioRecordListener {
        void startError();

        void wellPrepared();

        void recording(long recordtime,int decibel);

        void recordFinish(String path);
    }

    private static int BASE = 40;

    public void setOnAudioRecordListener(AudioRecordListener listener) {
        recordListener = listener;
    }

    /**
     * Get file path
     * @return
     */
    protected String generateAudioName() {
        File file = FileUtil.newContactFile(FileUtil.FileType.VOICE);
        return file.getAbsolutePath();
    }

    /**
     * Recording countdown timer
     */
    protected class RecordTimer extends CountDownTimer {

        public RecordTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (mediaRecorder != null) {
                int ratio = mediaRecorder.getMaxAmplitude() / BASE;
                int db = 0;
                if (ratio > 1) db = (int) (20 * Math.log10(ratio));
                recordListener.recording(RECORD_TOTAL - millisUntilFinished, db / 8);
            }
        }

        @Override
        public void onFinish() {
            finishRecorder();//60s Forced recording complete
        }
    }
}