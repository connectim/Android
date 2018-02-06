package connect.widget.zxing.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

import connect.activity.base.BaseApplication;
import connect.ui.activity.R;

/**
 * Generate two-dimensional code
 */
public class CreateScan {

    /** Default width */
    private int widthDef = 350;
    /** Default height */
    private int heightDef = 350;
    /** Default background */
    private int defaultBg = BaseApplication.getInstance().getResources().getColor(R.color.color_ffffff);

    public Bitmap generateQRCode(String content) {
        return generateQRCode(content,defaultBg);
    }

    public Bitmap generateQRCode(String content,int colorBg) {
        try {
            if(content == null){
                content = "";
            }
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

    private Bitmap bitMatrix2Bitmap(BitMatrix matrix, int colorBg) {
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

        Bitmap orginBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        orginBitmap.setPixels(rawData, 0, w, 0, 0, w, h);
        return orginBitmap;
    }


}
