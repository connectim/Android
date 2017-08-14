package connect.database.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;

import connect.activity.base.BaseApplication;
import connect.database.green.DaoHelper.ContactHelper;
import connect.im.bean.MsgType;
import connect.ui.activity.R;

@Entity
public class MessageEntity implements Serializable {

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;
    @NotNull
    @Unique
    private String message_ower;
    @NotNull
    @Unique
    private String message_id;

    private int chatType;
    private String from;
    private String to;
    private int messageType;

    private String content;
    private Long read_time;
    private Integer state;
    private Integer send_status;
    private Long snap_time;
    private Long createtime;

    @Generated(hash = 460439127)
    public MessageEntity(Long _id, @NotNull String message_ower,
            @NotNull String message_id, int chatType, String from, String to,
            int messageType, String content, Long read_time, Integer state,
            Integer send_status, Long snap_time, Long createtime) {
        this._id = _id;
        this.message_ower = message_ower;
        this.message_id = message_id;
        this.chatType = chatType;
        this.from = from;
        this.to = to;
        this.messageType = messageType;
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
    public int getChatType() {
        return this.chatType;
    }
    public void setChatType(int chatType) {
        this.chatType = chatType;
    }
    public String getFrom() {
        return this.from;
    }
    public void setFrom(String from) {
        this.from = from;
    }
    public String getTo() {
        return this.to;
    }
    public void setTo(String to) {
        this.to = to;
    }
    public int getMessageType() {
        return this.messageType;
    }
    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }


    /**
     * Display the list in the session
     *
     * @return
     */
    public String showContent() {
        String content = "";
        MsgType msgType = MsgType.toMsgType(getMessageType());
        switch (msgType) {
            case Text://text
                content = getContent();
                break;
            case Voice://voice
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Audio);
                break;
            case Photo://picture
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Picture);
                break;
            case Video://video
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Video);
                break;
            case Emotion://expression
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Expression);
                break;
            case Self_destruct_Notice://burn message
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Snapchat);
                break;
            case Self_destruct_Receipt://burn back
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Snapchat);
                break;
            case Request_Payment:
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Funding);
                break;
            case Transfer:
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Transfer);
                break;
            case Lucky_Packet:
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Red_packet);
                break;
            case Location:
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Location);
                break;
            case Name_Card:
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Visting_card);
                break;
            case INVITE_GROUP:
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Group_Namecard);
                break;
            case OUTER_WEBSITE:
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Group_certification);
                break;
            default:
                content = BaseApplication.getInstance().getBaseContext().getString(R.string.Chat_Tips);
                break;
        }

        switch (getChatType()) {
            case 0:
                break;
            case 1://show group member nickname
                GroupMemberEntity memberEntity = ContactHelper.getInstance().loadGroupMemberEntity(getTo(), getFrom());
                if (memberEntity != null) {
                    content = memberEntity.getUsername() + ": " + content;
                }
                break;
            case 2:
                break;
        }
        return content;
    }
}
