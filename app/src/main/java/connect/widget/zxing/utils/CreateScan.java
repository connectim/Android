package connect.widget.zxing.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import connect.ui.activity.R;
import connect.activity.base.BaseApplication;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

/**
 * Generate two-dimensional code
 */
public class CreateScan {

    /** Default width height */
    private int widthDef = 600;
    private int heightDef = 600;
    private int defultBg = BaseApplication.getInstance().getResources().getColor(R.color.color_ffffff);

    public Bitmap generateQRCode(String content) {
        return generateQRCode(content,defultBg);
    }

    public Bitmap generateQRCode(String content,int colorBg) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            Hashtable hints = new Hashtable();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            //Two dimensional code frame width, where the document says 0-4
            hints.put(EncodeHintType.MARGIN, 0);
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, widthDef, heightDef,hints);
            return bitMatrix2Bitmap(matrix,colorBg);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap bitMatrix2Bitmap(BitMatrix matrix,int colorBg) {
        int w = matrix.getWidth();
        int h = matrix.getHeight();
        int[] rawData = new int[w * h];
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int color = colorBg;
                if (matrix.get(i, j)) {
                    color = Color.BLACK;
                }
                rawData[i + (j * w)] = color;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        bitmap.setPixels(rawData, 0, w, 0, 0, w, h);
        return bitmap;
    }


}
