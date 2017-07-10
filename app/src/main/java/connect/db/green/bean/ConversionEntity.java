package connect.db.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ConversionEntity implements java.io.Serializable {

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;

    @Unique
    private String identifier;
    private Integer type;
    private String name;
    private String avatar;
    private String draft;
    private String content;
    private Integer unread_count;
    private Integer top;
    private Integer notice;
    private Integer stranger;
    private Long last_time;
    @Generated(hash = 2038489396)
    public ConversionEntity(Long _id, String identifier, Integer type, String name,
            String avatar, String draft, String content, Integer unread_count,
            Integer top, Integer notice, Integer stranger, Long last_time) {
        this._id = _id;
        this.identifier = identifier;
        this.type = type;
        this.name = name;
        this.avatar = avatar;
        this.draft = draft;
        this.content = content;
        this.unread_count = unread_count;
        this.top = top;
        this.notice = notice;
        this.stranger = stranger;
        this.last_time = last_time;
    }
    @Generated(hash = 1944361742)
    public ConversionEntity() {
    }
    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
    }
    public String getIdentifier() {
        return this.identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    public Integer getType() {
        return this.type;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAvatar() {
        return this.avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
    public String getDraft() {
        return this.draft;
    }
    public void setDraft(String draft) {
        this.draft = draft;
    }
    public String getContent() {
        return this.content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Integer getUnread_count() {
        return this.unread_count;
    }
    public void setUnread_count(Integer unread_count) {
        this.unread_count = unread_count;
    }
    public Integer getTop() {
        return this.top;
    }
    public void setTop(Integer top) {
        this.top = top;
    }
    public Integer getNotice() {
        return this.notice;
    }
    public void setNotice(Integer notice) {
        this.notice = notice;
    }
    public Integer getStranger() {
        return this.stranger;
    }
    public void setStranger(Integer stranger) {
        this.stranger = stranger;
    }
    public Long getLast_time() {
        return this.last_time;
    }
    public void setLast_time(Long last_time) {
        this.last_time = last_time;
    }

}
