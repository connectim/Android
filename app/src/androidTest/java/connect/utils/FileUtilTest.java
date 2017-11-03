package connect.utils;

import org.junit.Test;

import java.io.File;

import connect.database.SharedPreferenceUtil;

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
    public void newTempFile() throws Exception {
        File file = FileUtil.newTempFile(FileUtil.FileType.IMG);
        if(file == null){
            assertTrue(false);
        }else{
            assertTrue(true);
        }
    }

    @Test
    public void newContactFile() throws Exception {
        File file = FileUtil.newContactFile(FileUtil.FileType.IMG);
        if(file == null){
            assertTrue(false);
        }else{
            assertTrue(true);
        }
    }

    @Test
    public void islocalFile() throws Exception {
        boolean isLocal = FileUtil.isLocalFile(FileUtil.DIR_ROOT + File.separator + "aaa.png");
        assertTrue(isLocal);
    }

    @Test
    public void hasExtentsion() throws Exception {
        boolean isHave = FileUtil.hasExtentsion("aaa.png");
        assertTrue(isHave);
    }

    @Test
    public void subExtentsion() throws Exception {
        String name = FileUtil.subExtentsion("aaa.png");
        if (name.equals("aaa")) {
            assertTrue(true);
        } else {
            assertTrue(false);
        }
    }

    @Test
    public void deleteContactFile() throws Exception {
        String pubKey = SharedPreferenceUtil.getInstance().getUser().getPubKey();
        assertTrue(FileUtil.deleteContactFile(pubKey));
    }

}