package connect.db.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class MessageEntity implements Serializable {

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;

    @NotNull
    private String message_ower;

    @NotNull
    @Unique
    private String message_id;
    private String content;
    private Long read_time;
    private Integer state;
    private Integer send_status;
    private Long snap_time;
    private Long createtime;
    @Generated(hash = 467250247)
    public MessageEntity(Long _id, @NotNull String message_ower,
            @NotNull String message_id, String content, Long read_time,
            Integer state, Integer send_status, Long snap_time, Long createtime) {
        this._id = _id;
        this.message_ower = message_ower;
        this.message_id = message_id;
        this.content = content;
        this.read_time = read_time;
        this.state = state;
        this.send_status = send_status;
        this.snap_time = snap_time;
        this.createtime = createtime;
    }
    @Generated(hash = 1797882234)
    public MessageEntity() {
    }
    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
    }
    public String getMessage_ower() {
        return this.message_ower;
    }
    public void setMessage_ower(String message_ower) {
        this.message_ower = message_ower;
    }
    public String getMessage_id() {
        return this.message_id;
    }
    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Long getRead_time() {
        return this.read_time;
    }
    public void setRead_time(Long read_time) {
        this.read_time = read_time;
    }
    public Integer getState() {
        return this.state;
    }
    public void setState(Integer state) {
        this.state = state;
    }
    public Integer getSend_status() {
        return this.send_status;
    }
    public void setSend_status(Integer send_status) {
        this.send_status = send_status;
    }
    public Long getSnap_time() {
        return this.snap_time;
    }
    public void setSnap_time(Long snap_time) {
        this.snap_time = snap_time;
    }
    public Long getCreatetime() {
        return this.createtime;
    }
    public void setCreatetime(Long createtime) {
        this.createtime = createtime;
    }

}
