package connect.utils.log;

import android.util.Log;

/**
 * class that holds the {@link Logger} for this library, defaults to {@link LoggerDefault} to send logs to android {@link Log}
 */
public final class LogManager {

    private static Logger logger = new LoggerDefault();

    public static Logger getLogger() {
        return logger;
    }

}
