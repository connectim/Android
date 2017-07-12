package connect.utils;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Created by Administrator on 2017/7/10.
 */
public class FileUtilTest {

    @Test
    public void getExternalStorePathTest() throws Exception {
        String path = FileUtil.getExternalStorePath();
        assertTrue(path.length() > 0);
    }

    @Test
    public void randomFileNameTest() throws Exception {
        String name = FileUtil.randomFileName();
        assertTrue(name.length() > 0);
    }

    @Test
    public void newContactFile() throws Exception {
        File file = FileUtil.newContactFile(FileUtil.FileType.IMG);
        assertTrue(file.length() > 0);
    }

    @Test
    public void newTempFile() throws Exception {

    }

    @Test
    public void createNewFile() throws Exception {

    }

    @Test
    public void createAbsNewFile() throws Exception {

    }

    @Test
    public void newContactFileName() throws Exception {

    }

    @Test
    public void islocalFile() throws Exception {

    }

    @Test
    public void realFileName() throws Exception {

    }

    @Test
    public void isExistFilePath() throws Exception {

    }

    @Test
    public void isExistExternalStore() throws Exception {

    }

    @Test
    public void hasExtentsion() throws Exception {

    }

    @Test
    public void subExtentsion() throws Exception {

    }

    @Test
    public void byteArrayToFile() throws Exception {

    }

    @Test
    public void filePathToByteArray() throws Exception {

    }

    @Test
    public void fileSize() throws Exception {

    }

    @Test
    public void fileSize1() throws Exception {

    }

    @Test
    public void deleteContactFile() throws Exception {

    }

}