package connect.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2016/8/26.
 */
public class BitmapUtil {

    private static final String Tag = "BitmapUtil";
    public static final int smallWidth = 480;
    public static final int smallHeight = 800;
    public static final int bigWidth = 720;
    public static final int bigHeight = 1280;

    /**
     * Bitmap save to file
     */
    public static String bitmapSavePath(Bitmap bitmap) {
        return bitmapSavePath(bitmap,null,80);
    }

    public static String bitmapSavePathDCIM(Bitmap bitmap) {
        File appDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath(), "Camera");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        return bitmapSavePath(bitmap,file,100);
    }

    public static String bitmapSavePath(Bitmap bitmap,File file,int quality) {
        if(file == null)
            file = FileUtil.newTempFile(FileUtil.FileType.IMG);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        }  catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            bitmap.recycle();
        }
        return file.getPath();
    }

    /**
     * Image scaling (use Bitmap plus Matrix to zoom)
     */
    public static String resizeImage(String filePath, int w){
        Bitmap BitmapOrg = BitmapFactory.decodeFile(filePath);
        int width = BitmapOrg.getWidth();
        int height = BitmapOrg.getHeight();
        int newWidth = w;

        float scaleWidth = ((float) newWidth) / width;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleWidth);
        Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
                height, matrix, true);
        return bitmapSavePath(resizedBitmap,null,100);
    }

    public static Bitmap getSmallBitmap(String filePath, int reWidth, int reHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, reWidth, reHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }

    /**
     * Bitmap  to byte[]
     * @param bmp
     * @return
     */
    public static byte[] bmpToByteArray(final Bitmap bmp) {
        return bmpToByteArray(bmp,100);
    }

    public static byte[] bmpToByteArray(final Bitmap bmp,int quality) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, quality, output);
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            bmp.recycle();
        }
        return result;
    }

    /**
     * Get video thumbnails
     * @param filepath
     * @return
     */
    public static Bitmap thumbVideo(String filepath) {
        MediaMetadataRetriever media = new MediaMetadataRetriever();
        media.setDataSource(filepath);
        return media.getFrameAtTime();
    }

    public static byte[] InputStreamToByte(InputStream is) throws IOException {
        ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
        int ch;
        while ((ch = is.read()) != -1) {
            bytestream.write(ch);
        }
        byte imgdata[] = bytestream.toByteArray();
        bytestream.close();
        return imgdata;
    }

    public static Drawable getMaskDrawable(Context context, int maskId) {
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable = context.getDrawable(maskId);
        } else {
            drawable = context.getResources().getDrawable(maskId);
        }

        if (drawable == null) {
            throw new IllegalArgumentException("maskId is invalid");
        }
        return drawable;
    }
}
