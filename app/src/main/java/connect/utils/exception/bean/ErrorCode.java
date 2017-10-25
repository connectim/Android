package connect.utils.exception.bean;

/**
 * Created by Administrator on 2017/10/24.
 */

public enum ErrorCode {

    CONNECT_ERROR(400, "网络连接失败"),
    HTTP_SESSION_OVERDUE(401, "Session 过期"),
    USER_OFFSITE_LOGIN(402, "用户异地登录"),
    USER_NOT_EXIST(402, "用户不存在");

    int code;
    String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
