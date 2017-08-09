package connect.utils;

import org.junit.Test;

import java.io.File;
import java.security.SecureRandom;

import connect.utils.log.LogManager;
import connect.wallet.jni.AllNativeMethod;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Administrator on 2017/7/10.
 */
public class BitmapUtilTest {

    private String Tag = "_BitmapUtilTest";

    @Test
    public void compressTest() throws Exception {
        LogManager.getLogger().d(Tag, "compressTest");
        String path = "/mnt/sdcard/Ui.jpg";
        File file = BitmapUtil.getInstance().compress(path);
        assertTrue(file.length()>0);
    }

    @Test
    public void getImageSizeTest() throws Exception {
        LogManager.getLogger().d(Tag, "getImageSizeTest");
        String path = "/mnt/sdcard/Ui.jpg";
        int[] size = BitmapUtil.getInstance().getImageSize(path);
        assertTrue(size[0] > 0 && size[1] > 0);
    }
}