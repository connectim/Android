package connect.utils;

import android.text.TextUtils;

import org.junit.Test;

import connect.utils.log.LogManager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Administrator on 2017/7/10.
 */
public class StringUtilTest {

    private String Tag = "_StringUtilTest";

    @Test
    public void bytesToHexStringTest() throws Exception {
        byte[] bytes = {'1', '2', '3'};
        String hex = StringUtil.bytesToHexString(bytes);
        assertTrue(hex.length() >=6);
    }

    @Test
    public void hexStringToBytesTest() throws Exception {
        String testString ="313233";

        byte[] strBytes = testString.getBytes();
        LogManager.getLogger().d(Tag, "strBytes: " + strBytes[0] + strBytes[2]);
        String byteToHext = StringUtil.bytesToHexString(strBytes);
        byte[] hexToByte = StringUtil.hexStringToBytes(byteToHext);
        LogManager.getLogger().d(Tag, "hexToByte: " + hexToByte[0] + hexToByte[2]);

        byte[] bytes = StringUtil.hexStringToBytes("313233");
        assertTrue(bytes.length == 3);
    }

    @Test
    public void cdHash256() {
        String value = StringUtil.cdHash256("123");
        assertTrue(!TextUtils.isEmpty(value));
    }

    @Test
    public void hexStringToBytes() {
        String value = "123abc";
        byte[] byteValue = StringUtil.hexStringToBytes(value);
        if(byteValue.length != value.length()/2){
            assertTrue(false);
        }else{
            String value1 = StringUtil.bytesToHexString(byteValue);
            assertEquals(value1,value);
        }
    }

    @Test
    public void VersionComparison() {
        String ver1 = "1.2.2";
        String ver2 = "0.5.1";
        int value = StringUtil.VersionComparison(ver1, ver2); // 1
        int value1 = StringUtil.VersionComparison(ver1, ver1); // 0
        int value2 = StringUtil.VersionComparison(ver2, ver1); // -1
        if(value == 1 && value1 == 0 && value2 == -1){
            assertTrue(true);
        }else{
            assertTrue(false);
        }
    }

}