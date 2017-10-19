package connect.activity.set.bean;

/**
 * Created by Administrator on 2016/12/8.
 */
public class PrivateSetBean {

    private String updateTime;
    private Boolean phoneFind;
    private Boolean recommend;

    public PrivateSetBean() {}

    public PrivateSetBean(String updateTime, Boolean phoneFind,Boolean recommend) {
        this.updateTime = updateTime;
        this.phoneFind = phoneFind;
        this.recommend = recommend;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getPhoneFind() {
        return phoneFind;
    }

    public void setPhoneFind(Boolean phoneFind) {
        this.phoneFind = phoneFind;
    }

    public Boolean getRecommend() {
        return recommend;
    }

    public void setRecommend(Boolean recommend) {
        this.recommend = recommend;
    }

    public static PrivateSetBean initSetBean(){
        return new PrivateSetBean("",true,false);
    }
}
