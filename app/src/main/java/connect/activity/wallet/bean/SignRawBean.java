package connect.activity.wallet.bean;

/**
 * Created by Administrator on 2016/12/16.
 */
public class SignRawBean {

    private String hex;
    private boolean complete;

    public String getHex() {
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }
}
