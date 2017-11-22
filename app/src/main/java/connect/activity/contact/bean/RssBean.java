package connect.activity.contact.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/11/22 0022.
 */

public class RssBean implements Serializable{

    private long rssId;
    private String icon;
    private String title;
    private String desc;
    private boolean subRss; // 订阅状态 0 未订阅 1：已订阅

    public RssBean(long rssId, String icon, String title, String desc, boolean subRss) {
        this.rssId = rssId;
        this.icon = icon;
        this.title = title;
        this.desc = desc;
        this.subRss = subRss;
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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public boolean isSubRss() {
        return subRss;
    }

    public void setSubRss(boolean subRss) {
        this.subRss = subRss;
    }
}
