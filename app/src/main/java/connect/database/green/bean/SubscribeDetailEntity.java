package connect.database.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by Administrator on 2017/11/22.
 */
@Entity
public class SubscribeDetailEntity {

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;

    @NotNull
    @Unique
    private long messageId;

    @NotNull
    private long rssId;

    private Integer category;
    private String content;

    @Generated(hash = 304240773)
    public SubscribeDetailEntity() {
    }

    @Generated(hash = 496004816)
    public SubscribeDetailEntity(Long _id, long messageId, long rssId,
            Integer category, String content) {
        this._id = _id;
        this.messageId = messageId;
        this.rssId = rssId;
        this.category = category;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public long getRssId() {
        return rssId;
    }

    public void setRssId(long rssId) {
        this.rssId = rssId;
    }

    public Long get_id() {
        return this._id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }
}
