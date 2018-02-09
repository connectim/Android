package connect.utils.log;

import android.util.Log;

/**
 * Helper class to redirect {@link LogManager#logger} to {@link Log}
 */
public class LoggerDefault implements Logger {

    @Override
    public int v(String tag, String msg) {
        return debugEnade == false ? 0 : Log.v(tag, msg);
    }

    @Override
    public int v(String tag, String msg, Throwable tr) {
        return debugEnade == false ? 0 : Log.v(tag, msg, tr);
    }

    @Override
    public int d(String tag, String msg) {
        return debugEnade == false ? 0 : Log.d(tag, msg);
    }

    @Override
    public int d(String tag, String msg, Throwable tr) {
        return debugEnade == false ? 0 : Log.d(tag, msg, tr);
    }

    @Override
    public int i(String tag, String msg) {
        return debugEnade == false ? 0 : Log.i(tag, msg);
    }

    @Override
    public int i(String tag, String msg, Throwable tr) {
        return debugEnade == false ? 0 : Log.i(tag, msg, tr);
    }

    @Override
    public int w(String tag, String msg) {
        return debugEnade == false ? 0 : Log.w(tag, msg);
    }

    @Override
    public int w(String tag, String msg, Throwable tr) {
        return debugEnade == false ? 0 : Log.w(tag, msg, tr);
    }

    @Override
    public int e(String tag, String msg) {
        return debugEnade == false ? 0 : Log.e(tag, msg);
    }

    @Override
    public int e(String tag, String msg, Throwable tr) {
        return debugEnade == false ? 0 : Log.e(tag, msg, tr);
    }

    @Override
    public int l(String msg) {
        this.s(msg);
        return debugEnade == false ? 0 : Log.i("Logger", "\n" + msg);
    }

    @Override
    public void s(String msg) {
        if (debugEnade){
            System.out.println(msg);
        }
    }

    @Override
    public void s(String tag, String msg) {
        if (debugEnade){
            System.out.println(tag+"==>"+msg);
        }
    }

    @Override
    public int http(String msg) {
        return debugEnade == false ? 0 : Log.i("HTTP", "\n" + msg);
    }

    @Override
    public int local(String msg) {
        return debugEnade == false ? 0 : Log.i("LOCAL", "\n" + msg);
    }

    @Override
    public int im(String msg) {
        return debugEnade == false ? 0 : Log.i("IM", "\n" + msg);
    }
}
