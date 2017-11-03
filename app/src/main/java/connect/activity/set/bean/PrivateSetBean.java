package connect.activity.set.bean;

/**
 * Created by Administrator on 2016/12/8.
 */
public class PrivateSetBean {

    private Boolean phoneFind;
    private Boolean recommend;

    public PrivateSetBean() {}

    public PrivateSetBean(Boolean phoneFind,Boolean recommend) {
        this.phoneFind = phoneFind;
        this.recommend = recommend;
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
        return new PrivateSetBean(true,false);
    }
}
