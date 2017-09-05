package connect.utils.permission;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

/**
 * Determine the recording authority, compatible with the following android6.0 and above systems
 */

public class CheckAudioPermission {
    // Audio source
    public static int audioSource = MediaRecorder.AudioSource.MIC;
    // Set audio sampling rate, 44100 is the current standard, but some devices still support the 220501600011025
    public static int sampleRateInHz = 44100;
    // Set the audio recording of the channel CHANNEL_IN_STEREO for the dual channel, CHANNEL_CONFIGURATION_MONO for mono
    public static int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
    // Audio data format: PCM 16 bits per sample. Ensure equipment support. PCM 8 bits per sample. May not be able to get equipment support.
    public static int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    // Buffer byte size
    public static int bufferSizeInBytes = 0;
    /**
     * To determine whether there is a recording authority
     */
    public static boolean isHasPermission(){
        bufferSizeInBytes = 0;
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        //Start to record audio
        AudioRecord audioRecord = null;
        try{
            audioRecord =  new AudioRecord(audioSource, sampleRateInHz,
                    channelConfig, audioFormat, bufferSizeInBytes);
            // Prevent certain mobile collapse, such as lenovo
            audioRecord.startRecording();
        }catch (Exception e){
            e.printStackTrace();
        }
        /**
         * According to the beginning of recording to determine whether there is a recording authority
         */
        if (null == audioRecord || audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            return false;
        }
        audioRecord.stop();
        audioRecord.release();
        return true;
    }
}
