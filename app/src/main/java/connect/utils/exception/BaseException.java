package connect.utils.exception;

import com.tencent.bugly.crashreport.CrashReport;

import connect.utils.exception.bean.ErrorCode;
import connect.utils.exception.inter.ICrash;

/**
 * Created by Administrator on 2017/10/25.
 */
public class BaseException extends Throwable implements ICrash {

    private ErrorCode errorCode;

    public BaseException(ErrorCode errorCode) {
        super("ErrorCode :" + errorCode.getCode() + "  ErrorMessage:" + errorCode.getMessage());
        this.errorCode = errorCode;
    }

    @Override
    public BaseException dispath() {
        switch (errorCode) {
            case USER_NOT_EXIST:
                break;
            case HTTP_SESSION_OVERDUE:
                break;
            case USER_OFFSITE_LOGIN:
                break;
        }
        return this;
    }

    @Override
    public BaseException upload() {
        CrashReport.postCatchedException(this);
        return this;
    }
}
