package connect.utils;

import android.os.Handler;
import android.os.Message;

/**
 * Timer
 * Created by gtq on 2016/12/27.
 */
public abstract class ExCountDownTimer {

    /** tag running */
    private static final int MSG_RUN = 1;
    /** tag pause */
    private static final int MSG_PAUSE = 2;
    private final long mCountdownInterval;
    /** total time */
    private long mTotalTime;
    /** remain time */
    private long mRemainTime;

    public ExCountDownTimer(long millisInFuture, long countDownInterval) {
        mTotalTime = millisInFuture;
        mCountdownInterval = countDownInterval;
        mRemainTime = millisInFuture;
    }

    /** seek to progress */
    public final void seekTo(int value) {
        synchronized (ExCountDownTimer.this) {
            mRemainTime = ((100 - value) * mTotalTime) / 100;
        }
    }

    /** cancle timer */
    public final void cancel() {
        mHandler.removeMessages(MSG_RUN);
        mHandler.removeMessages(MSG_PAUSE);
    }

    /** Start time after pause */
    public final void resume() {
        mHandler.removeMessages(MSG_PAUSE);
        mHandler.sendMessageAtFrontOfQueue(mHandler.obtainMessage(MSG_RUN));
    }

    /** Pause timer */
    public final void pause() {
        onPause();

        mHandler.removeMessages(MSG_RUN);
        mHandler.sendMessageAtFrontOfQueue(mHandler.obtainMessage(MSG_PAUSE));
    }

    public synchronized final ExCountDownTimer start() {
        if (mRemainTime <= 0) {
            onFinish();
            return this;
        }

        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_RUN), mCountdownInterval);
        return this;
    }

    public abstract void onTick(long millisUntilFinished, int percent);

    public abstract void onPause();

    public abstract void onFinish();

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            synchronized (ExCountDownTimer.this) {
                if (msg.what == MSG_RUN) {
                    mRemainTime = mRemainTime - mCountdownInterval;

                    if (mRemainTime <= 0) {
                        onFinish();
                    } else if (mRemainTime < mCountdownInterval) {
                        sendMessageDelayed(obtainMessage(MSG_RUN), mRemainTime);
                    } else {
                        onTick(mRemainTime, Long.valueOf(100 * (mTotalTime - mRemainTime) / mTotalTime).intValue());
                        sendMessageDelayed(obtainMessage(MSG_RUN), mCountdownInterval);
                    }
                } else if (msg.what == MSG_PAUSE) {

                }
            }
        }
    };
}