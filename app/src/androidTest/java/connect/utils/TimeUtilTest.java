package connect.utils;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by Administrator on 2017/7/11.
 */
public class TimeUtilTest {

    @Test
    public void getTimeTest() throws Exception {
        String string = TimeUtil.getTime(1499738873, TimeUtil.DEFAULT_DATE_FORMAT);
        assertTrue(string.length() > 6);
    }

    @Test
    public void getCurrentTimeInLongTest() throws Exception {
        long timeLong = TimeUtil.getCurrentTimeInLong();
        assertTrue(timeLong > 1499738873);
    }

    @Test
    public void getCurrentTimeSecondTest() throws Exception {
        long timeLong = TimeUtil.getCurrentTimeSecond();
        assertTrue(timeLong > 1499738);
    }

    @Test
    public void getCurrentTimeInStringTest() throws Exception {
        String timeString = TimeUtil.getCurrentTimeInString(TimeUtil.DEFAULT_DATE_FORMAT);
        assertTrue(timeString.length() > 6);
    }

    @Test
    public void timestampToMsgidTest() throws Exception {
        String timeString = TimeUtil.timestampToMsgid();
        assertTrue(timeString.length() > 10);
    }

    @Test
    public void getMsgTimeTest() throws Exception {
        String timeString = TimeUtil.getMsgTime(1499738873);
        assertTrue(timeString.length() > 6);
    }


    @Test
    public void showTimeCountTest() throws Exception {
        String timeString = TimeUtil.showTimeCount(1499738873);
        assertTrue(timeString.length() == 8);
    }
}