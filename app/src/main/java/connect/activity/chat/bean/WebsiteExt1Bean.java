package connect.activity.chat.bean;

import java.io.Serializable;

/**
 * Created by pujin on 2017/2/20.
 */
public class WebsiteExt1Bean implements Serializable{
    private String linkTitle;
    private String linkSubtitle;
    private String linkImg;

    public WebsiteExt1Bean() {
    }

    public WebsiteExt1Bean(String linkTitle, String linkSubtitle, String linkImg) {
        this.linkTitle = linkTitle;
        this.linkSubtitle = linkSubtitle;
        this.linkImg = linkImg;
    }

    public String getLinkTitle() {
        return linkTitle;
    }

    public void setLinkTitle(String linkTitle) {
        this.linkTitle = linkTitle;
    }

    public String getLinkSubtitle() {
        return linkSubtitle;
    }

    public void setLinkSubtitle(String linkSubtitle) {
        this.linkSubtitle = linkSubtitle;
    }

    public String getLinkImg() {
        return linkImg;
    }

    public void setLinkImg(String linkImg) {
        this.linkImg = linkImg;
    }

    public void setExt1(String title, String sub) {
        this.linkTitle = title;
        this.linkSubtitle = sub;
    }
}
