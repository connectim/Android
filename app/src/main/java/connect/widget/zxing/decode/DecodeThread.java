package connect.widget.zxing.decode;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.CountDownLatch;
import connect.activity.base.BaseScanActivity;

/**
 * This thread does all the heavy lifting of decoding the images.
 */
public final class DecodeThread extends Thread {

    private final BaseScanActivity mActivity;
    private final CountDownLatch mHandlerInitLatch;
    private Handler mHandler;

    public DecodeThread(BaseScanActivity activity) {
        this.mActivity = activity;
        mHandlerInitLatch = new CountDownLatch(1);
    }

    public Handler getHandler() {
        try {
            mHandlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return mHandler;
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new DecodeHandler(mActivity);
        mHandlerInitLatch.countDown();
        Looper.loop();
    }
}
