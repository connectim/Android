package connect.activity.chat.bean;

import android.text.TextUtils;

import java.io.Serializable;

import protos.Connect;

/**
 * Created by pujin on 2017/3/24.
 */

public class AdBean implements Serializable{
    private String title;
    private String content;
    private int category;//0:link 1:upgrade
    private long createTime;
    private String url;
    private String conversUrl;

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

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getConversUrl() {
        return conversUrl;
    }

    public void setConversUrl(String conversUrl) {
        this.conversUrl = conversUrl;
    }

    public void transSystemAd(Connect.Announcement announcement){
        setCategory(announcement.getCategory());
        setContent(TextUtils.isEmpty(announcement.getContent())?announcement.getDesc():announcement.getContent());
        setConversUrl(announcement.getCoversUrl());
        setCreateTime((long)announcement.getCreatedAt());
        setTitle(announcement.getTitle());
        setUrl(announcement.getUrl());
    }
}
