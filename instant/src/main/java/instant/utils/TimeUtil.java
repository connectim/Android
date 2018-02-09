package instant.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

/**
 * time tool
 */
public class TimeUtil {

    public static final SimpleDateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
    public static final SimpleDateFormat DATE_FORMAT_DATE = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
    public static final SimpleDateFormat DATE_FORMAT_MONTH = new SimpleDateFormat("MM-dd", Locale.ENGLISH);
    public static final SimpleDateFormat DATE_FORMAT_SECOND = new SimpleDateFormat("mm:ss", Locale.ENGLISH);
    public static final SimpleDateFormat DATE_FORMAT_HOUR_MIN = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    public static final SimpleDateFormat DATE_FORMAT_HOUR_MIN_SCE = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
    public static final SimpleDateFormat DATE_FORMAT_MONTH_HOUR = new SimpleDateFormat("MM-dd HH:mm", Locale.ENGLISH);

    /**
     * long time to string
     * @param timeInMillis
     * @param dateFormat
     * @return
     */
    public static String getTime(long timeInMillis, SimpleDateFormat dateFormat) {
        return dateFormat.format(new Date(timeInMillis));
    }

    /**
     * get current time in milliseconds
     * @return
     */
    public static long getCurrentTimeInLong() {
        return System.currentTimeMillis();
    }

    public static long getCurrentTimeSecond() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * get current time in milliseconds
     * @return
     */
    public static String getCurrentTimeInString(SimpleDateFormat dateFormat) {
        return dateFormat.format(new Date(getCurrentTimeInLong()));
    }

    /**
     * timestamp to messageid
     * @return
     */
    public static String timestampToMsgid() {
        Random rm = new Random();
        double pross = (1 + rm.nextDouble()) * Math.pow(10, 3);
        return getCurrentTimeInLong() + String.valueOf(pross).substring(1, 3 + 1);
    }

    public static int msgidToInt(String msgid) {
        return Integer.parseInt(msgid.substring(8));
    }
}
