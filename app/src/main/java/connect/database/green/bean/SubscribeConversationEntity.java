package connect.database.green.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/11/22.
 */
@Entity
public class SubscribeConversationEntity implements Serializable {

    static final long serialVersionUID = 42L;

    @Id(autoincrement = true)
    private Long _id;

    @NotNull
    @Unique
    private long rssId;

    private String icon;
    private String title;
    private String content;
    private long time;
    private int unRead;

    @Generated(hash = 2064457384)
    public SubscribeConversationEntity() {
    }

    @Generated(hash = 443537533)
    public SubscribeConversationEntity(Long _id, long rssId, String icon,
            String title, String content, long time, int unRead) {
        this._id = _id;
        this.rssId = rssId;
        this.icon = icon;
        this.title = title;
        this.content = content;
        this.time = time;
        this.unRead = unRead;
    }

    public long getRssId() {
        return rssId;
    }

    public void setRssId(long rssId) {
        this.rssId = rssId;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Long get_id() {
        return this._id;
    }

    public void set_id(Long _id) {
        this._id = _id;
    }

    public int getUnRead() {
        return unRead;
    }

    public void setUnRead(int unRead) {
        this.unRead = unRead;
    }
}
