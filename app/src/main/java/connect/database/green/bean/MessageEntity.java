package connect.database.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;

import connect.activity.base.BaseApplication;
import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.bean.MsgExtEntity;
import connect.database.MemoryDataManager;
import connect.database.green.DaoHelper.ContactHelper;
import connect.im.bean.MsgType;
import connect.ui.activity.R;
import protos.Connect;

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

    private int chatType;
    private String message_from;
    private String message_to;
    private int messageType;

    private String content;
    private Long read_time;
    private Integer send_status;
    private Long snap_time;
    private Long createtime;


    @Generated(hash = 814481234)
    public MessageEntity(Long _id, @NotNull String message_ower, @NotNull String message_id, int chatType,
            String message_from, String message_to, int messageType, String content, Long read_time, Integer send_status,
            Long snap_time, Long createtime) {
        this._id = _id;
        this.message_ower = message_ower;
        this.message_id = message_id;
        this.chatType = chatType;
        this.message_from = message_from;
        this.message_to = message_to;
        this.messageType = messageType;
        this.content = content;
        this.read_time = read_time;
        this.send_status = send_status;
        this.snap_time = snap_time;
        this.createtime = createtime;
    }

    @Generated(hash = 1797882234)
    public MessageEntity() {
    }

    public MsgExtEntity transToExtEntity() {
        MsgExtEntity extEntity = new MsgExtEntity();
        extEntity.set_id(get_id());
        extEntity.setMessage_id(getMessage_id());
        extEntity.setChatType(getChatType());
        extEntity.setMessage_ower(getMessage_ower());
        extEntity.setMessageType(getMessageType());
        extEntity.setMessage_from(getMessage_from());
        extEntity.setMessage_to(getMessage_to());
        extEntity.setContent(getContent());
        extEntity.setCreatetime(getCreatetime());
        extEntity.setRead_time(getRead_time());
        extEntity.setSend_status(getSend_status());
        extEntity.setSnap_time(getSnap_time());
        return extEntity;
    }

    public MsgDirect parseDirect() {
        String mypubkey = MemoryDataManager.getInstance().getPubKey();
        return mypubkey.equals(getMessage_from()) ? MsgDirect.To : MsgDirect.From;
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

    public int getChatType() {
        return this.chatType;
    }

    public void setChatType(int chatType) {
        this.chatType = chatType;
    }

    public String getMessage_from() {
        return this.message_from;
    }

    public void setMessage_from(String message_from) {
        this.message_from = message_from;
    }

    public String getMessage_to() {
        return this.message_to;
    }

    public void setMessage_to(String message_to) {
        this.message_to = message_to;
    }

    public int getMessageType() {
        return this.messageType;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
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
