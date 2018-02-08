package connect.utils;

import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import connect.activity.base.BaseApplication;
import connect.ui.activity.R;

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

    public static String getMsgTime(long timestamp) throws Exception {
        return getMsgTime(getCurrentTimeInLong(), timestamp);
    }

    /**
     * Timestamp to descriptive time
     * @param lastTime
     * @param msgTime
     * @return
     * @throws Exception
     */
    public static String getMsgTime(long lastTime,long msgTime) throws Exception {
        Calendar msgcalendar = Calendar.getInstance();
        msgcalendar.setTimeInMillis(lastTime);
        Calendar curcalendar = Calendar.getInstance();
        curcalendar.setTimeInMillis(msgTime);

        SimpleDateFormat format = null;
        String showTime = "";

        int msgYear = msgcalendar.get(Calendar.YEAR);
        int curYear = curcalendar.get(Calendar.YEAR);
        if (curYear != msgYear) {
            format = DATE_FORMAT_DATE;
            showTime = format.format(msgTime);
        } else {
            int msgMonth = msgcalendar.get(Calendar.MONTH);
            int curMonth = curcalendar.get(Calendar.MONTH);
            if (msgMonth != curMonth) {
                format = DATE_FORMAT_MONTH;
                showTime = format.format(msgTime);
            } else {
                Context context = BaseApplication.getInstance().getBaseContext();

                int msgDay = msgcalendar.get(Calendar.DAY_OF_MONTH);
                int curDay = curcalendar.get(Calendar.DAY_OF_MONTH);
                int spaceDay = Math.abs(curDay - msgDay);
                switch (spaceDay) {
                    case 0:
                        format = DATE_FORMAT_HOUR_MIN;
                        showTime = format.format(msgTime);
                        break;
                    case 1:
                        format = DATE_FORMAT_HOUR_MIN;
                        showTime = format.format(msgTime);
                        showTime = context.getString(R.string.Chat_Yesterday) + " " + showTime;
                        break;
                    case 2:
                        format = DATE_FORMAT_HOUR_MIN;
                        showTime = format.format(msgTime);
                        showTime = context.getString(R.string.Chat_the_day_before_yesterday_time, " " + showTime);
                        break;
                    default:
                        format = DATE_FORMAT_HOUR_MIN;
                        showTime = format.format(msgTime);
                        break;
                }
            }
        }
        return showTime;
    }

    public static String getRobotContentMsgTime(long lastTime,long msgTime) throws Exception {
        Calendar msgcalendar = Calendar.getInstance();
        msgcalendar.setTimeInMillis(lastTime);
        Calendar curcalendar = Calendar.getInstance();
        curcalendar.setTimeInMillis(msgTime);

        SimpleDateFormat format = null;
        String showTime = "";

        int msgYear = msgcalendar.get(Calendar.YEAR);
        int curYear = curcalendar.get(Calendar.YEAR);
        if (curYear != msgYear) {
            format = DATE_FORMAT_DATE;
            showTime = format.format(msgTime);
        } else {
            int msgMonth = msgcalendar.get(Calendar.MONTH);
            int curMonth = curcalendar.get(Calendar.MONTH);
            if (msgMonth != curMonth) {
                format = DATE_FORMAT_MONTH;
                showTime = format.format(msgTime);
            } else {
                Context context = BaseApplication.getInstance().getBaseContext();

                int msgDay = msgcalendar.get(Calendar.DAY_OF_MONTH);
                int curDay = curcalendar.get(Calendar.DAY_OF_MONTH);
                int spaceDay = Math.abs(curDay - msgDay);
                switch (spaceDay) {
                    case 0:
                        format = DATE_FORMAT_HOUR_MIN;
                        showTime = context.getString(R.string.Link_Today) + "  " + format.format(msgTime);
                        break;
                    case 1:
                        format = DATE_FORMAT_HOUR_MIN;
                        showTime = format.format(msgTime);
                        showTime = context.getString(R.string.Chat_Yesterday) + " " + showTime;
                        break;
                    case 2:
                        format = DATE_FORMAT_HOUR_MIN;
                        showTime = format.format(msgTime);
                        showTime = context.getString(R.string.Chat_the_day_before_yesterday_time, " " + showTime);
                        break;
                    default:
                        format = DATE_FORMAT_HOUR_MIN;
                        showTime = format.format(msgTime);
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

    private static Map<String, String> burnTimeMap=new HashMap<>();

    /**
     * Burn after reading time
     *
     * @param time
     * @return
     */
    public static String parseBurnTime(int time) {
        if (burnTimeMap == null || burnTimeMap.isEmpty()) {
            burnTimeMap = new HashMap<>();

            Context context = BaseApplication.getInstance().getBaseContext();
            int[] destimes = context.getResources().getIntArray(R.array.destruct_timer_long);
            String[] strtimes = context.getResources().getStringArray(R.array.destruct_timer);
            for (int i = 0; i < destimes.length; i++) {
                burnTimeMap.put(String.valueOf(destimes[i]), strtimes[i]);
            }
        }
        return burnTimeMap.get(String.valueOf(time));
    }
}
