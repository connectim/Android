package connect.widget.zxing.decode;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import connect.activity.base.BaseScanActivity;
import connect.ui.activity.R;

final class DecodeHandler extends Handler {

    private final BaseScanActivity mActivity;
    private final MultiFormatReader mMultiFormatReader;
    private final Map<DecodeHintType, Object> mHints;
    private byte[] mRotatedData;

    DecodeHandler(BaseScanActivity activity) {
        this.mActivity = activity;
        mMultiFormatReader = new MultiFormatReader();
        mHints = new Hashtable<>();
        mHints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        mHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        Collection<BarcodeFormat> barcodeFormats = new ArrayList<>();
        barcodeFormats.add(BarcodeFormat.QR_CODE);
        barcodeFormats.add(BarcodeFormat.CODE_39);
        barcodeFormats.add(BarcodeFormat.CODE_93);
        barcodeFormats.add(BarcodeFormat.CODE_128);
        mHints.put(DecodeHintType.POSSIBLE_FORMATS, barcodeFormats);
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.decode:
                decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case R.id.quit:
                Looper looper = Looper.myLooper();
                if (null != looper) {
                    looper.quit();
                }
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency, reuse the same reader
     * objects from one decode to the next.
     *
     * @param data The YUV preview frame.
     * @param width The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        if (null == mRotatedData) {
            mRotatedData = new byte[width * height];
        } else {
            if (mRotatedData.length < width * height) {
                mRotatedData = new byte[width * height];
            }
        }
        Arrays.fill(mRotatedData, (byte) 0);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x + y * width >= data.length) {
                    break;
                }
                mRotatedData[x * height + height - y - 1] = data[x + y * width];
            }
        }
        // Here we are swapping, that's the difference to
        int tmp = width;
        width = height;
        height = tmp;

        Result rawResult = null;
        try {
            PlanarYUVLuminanceSource source =
                    new PlanarYUVLuminanceSource(mRotatedData, width, height, 0, 0, width, height, false);
            BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
            rawResult = mMultiFormatReader.decode(bitmap1, mHints);
        } catch (ReaderException ignored) {

        } finally {
            mMultiFormatReader.reset();
        }

        Handler handler = mActivity.getHandler();
        if (rawResult != null && handler != null) {
            Message message = Message.obtain(handler, R.id.decode_succeeded, rawResult);
            message.sendToTarget();
        }else if (handler != null){
            Message message = Message.obtain(handler, R.id.decode_failed);
            message.sendToTarget();
        }
    }
}
