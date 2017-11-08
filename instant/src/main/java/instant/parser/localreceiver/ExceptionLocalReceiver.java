package instant.parser.localreceiver;

import instant.parser.inter.ExceptionListener;

/**
 * Created by Administrator on 2017/11/8.
 */

public class ExceptionLocalReceiver implements ExceptionListener {

    public static ExceptionLocalReceiver localReceiver = getInstance();

    private synchronized static ExceptionLocalReceiver getInstance() {
        if (localReceiver == null) {
            localReceiver = new ExceptionLocalReceiver();
        }
        return localReceiver;
    }

    private ExceptionListener exceptionListener = null;

    public void registerConnect(ExceptionListener listener) {
        this.exceptionListener = listener;
    }

    public ExceptionListener getExceptionListener() {
        if (exceptionListener == null) {
            throw new RuntimeException("exceptionListener don't register");
        }
        return exceptionListener;
    }

    @Override
    public void exitAccount() {
        getExceptionListener().exitAccount();
    }
}
