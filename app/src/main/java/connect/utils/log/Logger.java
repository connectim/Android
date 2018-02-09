package connect.utils.log;

/**
 * interface for a logger class to replace the static calls to {@link android.util.Log}
 */
public interface Logger {

    /**
     *  debug true :print log， false :close log
     */
//    boolean debugEnade = !ConfigUtil.getInstance().appMode();
    boolean debugEnade = true;
    /**
     * Send a {@link android.util.Log#VERBOSE} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    int v(String tag, String msg);

    /**
     * Send a {@link android.util.Log#VERBOSE} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    int v(String tag, String msg, Throwable tr);

    /**
     * Send a {@link android.util.Log#DEBUG} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    int d(String tag, String msg);

    /**
     * Send a {@link android.util.Log#DEBUG} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    int d(String tag, String msg, Throwable tr);

    /**
     * Send an {@link android.util.Log#INFO} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    int i(String tag, String msg);

    /**
     * Send a {@link android.util.Log#INFO} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    int i(String tag, String msg, Throwable tr);

    /**
     * Send a {@link android.util.Log#WARN} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    int w(String tag, String msg);

    /**
     * Send a {@link android.util.Log#WARN} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    int w(String tag, String msg, Throwable tr);

    /**
     * Send an {@link android.util.Log#ERROR} log message.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     */
    int e(String tag, String msg);

    /**
     * Send a {@link android.util.Log#ERROR} log message and log the exception.
     *
     * @param tag Used to identify the source of a log message.  It usually identifies
     *            the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    int e(String tag, String msg, Throwable tr);

    /**
     *  Log.i("Logger","msg");
     * @param msg
     * @return
     */
    int l(String msg);

    /***
     * System.out.println("msg");
     * @param msg The message you would like logged.
     */
    void s(String msg);
    /***
     * System.out.println("tag ==> msg");
     * @param tag Used to identify the source of a log message.
     * @param msg The message you would like logged.
     */
    void s(String tag, String msg);

    /**
     *  Log.i("IM","msg");
     * @param msg
     * @return
     */
    int im(String msg);

    /**
     *  Log.i("HTTP","msg");
     * @param msg
     * @return
     */
    int http(String msg);


    /**
     *  Log.i("LOCAL","msg");
     * @param msg
     * @return
     */
    int local(String msg);
}
