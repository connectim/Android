package connect.utils;

import android.content.Context;

import connect.ui.activity.R;
import connect.ui.base.BaseApplication;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    /**
     * Timestamp to descriptive time
     * @param msgtime
     * @return
     * @throws Exception
     */
    public static String getMsgTime(long msgtime) throws Exception {
        return getMsgTime(TimeUtil.getCurrentTimeInLong() - 3 * 1000, msgtime);
    }

    /**
     * Timestamp to descriptive time
     * @param lasttime
     * @param msgtime
     * @return
     * @throws Exception
     */
    public static String getMsgTime(long lasttime,long msgtime) throws Exception {
        Calendar msgcalendar = Calendar.getInstance();
        msgcalendar.setTimeInMillis(lasttime);
        Calendar curcalendar = Calendar.getInstance();
        curcalendar.setTimeInMillis(msgtime);

        SimpleDateFormat format = null;
        String showTime = "";

        int msgYear = msgcalendar.get(Calendar.YEAR);
        int curYear = curcalendar.get(Calendar.YEAR);
        if (curYear != msgYear) {
            format = DATE_FORMAT_DATE;
            showTime = format.format(msgtime);
        } else {
            int msgMonth = msgcalendar.get(Calendar.MONTH);
            int curMonth = curcalendar.get(Calendar.MONTH);
            if (msgMonth != curMonth) {
                format = DATE_FORMAT_MONTH;
                showTime = format.format(msgtime);
            } else {
                Context context = BaseApplication.getInstance().getBaseContext();

                int msgDay = msgcalendar.get(Calendar.DAY_OF_MONTH);
                int curDay = curcalendar.get(Calendar.DAY_OF_MONTH);
                switch (curDay - msgDay) {
                    case 0:
                        format = DATE_FORMAT_HOUR_MIN;
                        showTime = format.format(msgtime);
                        break;
                    case 1:
                        format = DATE_FORMAT_HOUR_MIN;
                        showTime = format.format(msgtime);
                        showTime = context.getString(R.string.Chat_Yesterday)+" "+showTime;
                        break;
                    case 2:
                        format = DATE_FORMAT_HOUR_MIN;
                        showTime = format.format(msgtime);
                        showTime = context.getString(R.string.Chat_the_day_before_yesterday_time, " " + showTime);
                        break;
                    default:
                        format = DATE_FORMAT_HOUR_MIN;
                        showTime = format.format(msgtime);
                        break;
                }
            }
        }
        return showTime;
    }

    public static String showTimeCount(long time) {
        if (time >= 360000000) {
            return "00:00:00";
        }
        String timeCount = "";
        long hourc = time / 3600000;
        String hour = "0" + hourc;
        hour = hour.substring(hour.length() - 2, hour.length());

        long minuec = (time - hourc * 3600000) / (60000);
        String minue = "0" + minuec;
        minue = minue.substring(minue.length() - 2, minue.length());

        long secc = (time - hourc * 3600000 - minuec * 60000) / 1000;
        String sec = "0" + secc;
        sec = sec.substring(sec.length() - 2, sec.length());
        timeCount = hour + ":" + minue + ":" + sec;
        return timeCount;
    }

}
