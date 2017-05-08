package connect.ui.activity.chat.bean;

/**
 * Created by gtq on 2016/12/2.
 */
public class MsgEntity extends BaseEntity {
    private String avatar;
    private String recAddress;
    private long burnstarttime;


    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRecAddress() {
        return recAddress;
    }

    public void setRecAddress(String recAddress) {
        this.recAddress = recAddress;
    }

    public long getBurnstarttime() {
        return burnstarttime;
    }

    public void setBurnstarttime(long burnstarttime) {
        this.burnstarttime = burnstarttime;
    }
}