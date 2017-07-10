package connect.activity.chat.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import connect.ui.activity.R;
import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.bean.RecExtBean;
import connect.utils.MediaUtil;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;

/**
 * Created by gtq on 2016/11/26.
 */
public class VoiceImg extends ImageView {

    private Context context;
    private Paint txtPaint = new Paint();
    private Paint mPaint = new Paint();
    private long voiceLength;
    private String voicePath = "";
    private Timer timer;
    private TimerTask timerTask;
    private int imgRes = R.mipmap.voice_play_icon2x;
    private String msgid;
    private MsgDirect msgDirect;

    public VoiceImg(Context context) {
        super(context);
        initView();
    }

    public VoiceImg(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public VoiceImg(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    protected void initView() {
        context = getContext();
        txtPaint.setColor(getResources().getColor(R.color.color_0da835));
        txtPaint.setTextSize(SystemUtil.dipToPx(16));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RecExtBean extBean) {
        Object[] objects = (Object[]) extBean.getObj();
        String filepath = null;
        switch (extBean.getExtType()) {
            case VOICE_COMPLETE://complete
                filepath = (String) objects[0];
                if (filepath.equals(voicePath)) {
                    stopPlay();
                    if (!TextUtils.isEmpty(msgid)) {
                        RecExtBean.sendRecExtMsg(RecExtBean.ExtType.VOICE_UNREAD, msgid);
                    }
                }
                break;
            case VOICE_RELEASE://release
                filepath = (String) objects[0];
                if (!TextUtils.isEmpty(filepath) && filepath.equals(voicePath)) {
                    stopPlay();
                }
                break;
        }
    }

    public void loadVoice(MsgDirect direct ,int sec) {
        this.msgDirect = direct;
        this.voiceLength = sec;
        this.setLayoutParams(new RelativeLayout.LayoutParams(calculateBubbleWidth(sec, 60), SystemUtil.dipToPx(40)));
    }

    /**
     * Calculate length of voice
     * @param seconds
     * @param MAX_TIME
     * @return
     */
    private int calculateBubbleWidth(long seconds, int MAX_TIME) {
        int maxAudioBubbleWidth = getAudioMaxEdge();
        int minAudioBubbleWidth = getAudioMinEdge();

        int currentBubbleWidth;
        if (seconds <= 0) {
            currentBubbleWidth = minAudioBubbleWidth;
        } else if (seconds > 0 && seconds <= MAX_TIME) {
            currentBubbleWidth = (int) ((maxAudioBubbleWidth - minAudioBubbleWidth) * (2.0 / Math.PI)
                    * Math.atan(seconds / 10.0) + minAudioBubbleWidth);
        } else {
            currentBubbleWidth = maxAudioBubbleWidth;
        }

        if (currentBubbleWidth < minAudioBubbleWidth) {
            currentBubbleWidth = minAudioBubbleWidth;
        } else if (currentBubbleWidth > maxAudioBubbleWidth) {
            currentBubbleWidth = maxAudioBubbleWidth;
        }

        return currentBubbleWidth;
    }

    public static int getAudioMaxEdge() {
        return (int) (0.6 * SystemDataUtil.getScreenWidth());
    }

    public static int getAudioMinEdge() {
        return (int) (0.1875 * SystemDataUtil.getScreenWidth());
    }

    private final int VOICE_FREQUENCY = 500;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 100:
                    int counttime = msg.arg1;
                    switch ((counttime / VOICE_FREQUENCY) % 3) {
                        case 0:
                            if (msgDirect == MsgDirect.To) {
                                imgRes = R.mipmap.voice_to1;
                            } else {
                                imgRes = R.mipmap.voice_from1;
                            }
                            break;
                        case 1:
                            if (msgDirect == MsgDirect.To) {
                                imgRes = R.mipmap.voice_to2;
                            } else {
                                imgRes = R.mipmap.voice_from2;
                            }
                            break;
                        case 2:
                            if (msgDirect == MsgDirect.To) {
                                imgRes = R.mipmap.voice_to3;
                            } else {
                                imgRes = R.mipmap.voice_from3;
                            }
                            break;
                    }
                    invalidate();
                    break;
                case 150:
                    stopPlay();
                    if (playListener != null) {
                        playListener.playFinish(msgid, voicePath);
                    }
                    break;
            }
        }
    };

    public void startPlay(String path) {
        startPlay(null, path);
    }

    public void startPlay(String msgid, String path) {
        startPlay(msgid, path, null);
    }

    public void startPlay(String msgid, String path,VoicePlayListener listener) {
        this.msgid = msgid;
        this.voicePath = path;
        this.playListener = listener;

        cancelTimer();
        if (MediaUtil.getInstance().isPlayVoive()) {
            if (MediaUtil.getInstance().getFilePath().equals(path)) {
                stopPlay();
                MediaUtil.getInstance().freeMediaPlayerResource();
                return;
            }
        }
        timer = new Timer();
        timerTask = new TimerTask() {
            private int counttime = 0;

            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 100;
                msg.arg1 = counttime;

                counttime += VOICE_FREQUENCY;
                if (counttime >= voiceLength * 1000) {
                    msg.what = 150;
                }
                mHandler.sendMessage(msg);
            }
        };
        timer.schedule(timerTask, VOICE_FREQUENCY, VOICE_FREQUENCY);
        MediaUtil.getInstance().playVoice(path);
    }

    public void stopPlay() {
        cancelTimer();
        mHandler.removeMessages(100);
        imgRes = R.mipmap.voice_play_icon2x;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawText(canvas);
        drawBitmap(canvas);
    }

    private void drawText(Canvas canvas) {
        String format = String.format(Locale.ENGLISH,"%1$02d:%2$02d", voiceLength / 60, voiceLength % 60);
        int txtHeight = (int) ((getHeight() / 2.0f) - ((txtPaint.descent() + txtPaint.ascent()) / 2.0f));
        canvas.drawText(format, getWidth() - 2 * SystemUtil.dipToPx(17) - SystemUtil.dipToPx(23), txtHeight, txtPaint);
    }

    private void drawBitmap(Canvas canvas) {
        if (imgRes != -1) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imgRes);
            canvas.drawBitmap(bitmap, SystemUtil.dipToPx(15), (getHeight() - bitmap.getHeight()) / 2, mPaint);
        }
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask.cancel();
        }
    }

    private VoicePlayListener playListener = null;

    public interface VoicePlayListener {
        void playFinish(String msgid, String filepath);
    }

    public void setPlayListener(VoicePlayListener playListener) {
        this.playListener = playListener;
    }

    public void downLoading() {
        imgRes = -1;
        invalidate();
    }
}