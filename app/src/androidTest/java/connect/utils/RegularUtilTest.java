package connect.utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Administrator on 2017/7/10.
 */
public class RegularUtilTest {

    private String Tag="RegularUtilTest";

    private RegularUtil regularUtil;

    @Before
    public void setUp() throws Exception {
        regularUtil = new RegularUtil();
    }

    @Test
    public void matchesTest() throws Exception {
        assertEquals("123456", regularUtil.PHONE_NUMBER, 0);
    }

    @Test
    public void spliteTest() throws Exception {
        String[] strings = RegularUtil.splite("123u456", "u");
        assertEquals("123",strings[0]);
    }

    @Test
    public void replaceTest() throws Exception {
        String temp = RegularUtil.replace("123u456", "u", "y");
        assertEquals("123y456", temp);
    }

    @Test
    public void groupAvatarTest() throws Exception {

    }
}