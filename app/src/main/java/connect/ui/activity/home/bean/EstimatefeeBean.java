package connect.ui.activity.home.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2016/12/16.
 */
public class EstimatefeeBean implements Serializable{

    /**
     * code : 2000
     * data : 0.000130
     * message : success
     */

    private int code;
    private String data;
    private String message;
    private Long time;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
