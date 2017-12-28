package connect.utils;

import org.junit.Test;

import java.io.File;

import connect.utils.log.LogManager;

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

}