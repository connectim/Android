package connect.ui.activity.chat.bean;

import java.io.Serializable;

/**
 * Created by pujin on 2017/1/22.
 */

public class ApplyGroupBean implements Serializable{
    private String tips;
    private int source;
    private int state;
    private String msgid;

    public ApplyGroupBean(String tips, int source, int state, String msgid) {
        this.tips = tips;
        this.source = source;
        this.state = state;
        this.msgid = msgid;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getMsgid() {
        return msgid;
    }

    public void setMsgid(String msgid) {
        this.msgid = msgid;
    }
}
