package connect.utils;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by Administrator on 2017/7/10.
 */
public class StringUtilTest {

    @Test
    public void bytesToHexStringTest() throws Exception {
        byte[] bytes = {'1', '2', '3'};
        String hex = StringUtil.bytesToHexString(bytes);
        assertTrue(hex.length() >=6);
    }

    @Test
    public void hexStringToBytesTest() throws Exception {
        byte[] bytes = StringUtil.hexStringToBytes("313233");
        assertTrue(bytes.length == 3);
    }

    @Test
    public void byteTomd5Test() throws Exception {
        byte[] bytes = {'1', '2', '3'};
        byte[] md5 = StringUtil.byteTomd5(bytes);
        assertTrue(bytes.length == 3);
    }
}