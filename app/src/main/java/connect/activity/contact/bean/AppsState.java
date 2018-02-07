package connect.activity.contact.bean;

/**
 * Created by PuJin on 2018/1/17.
 */

public class AppsState {

    public enum AppsEnum{
        APPLICATION,
    }

    private AppsState.AppsEnum appsEnum;

    public AppsState() {}

    public AppsState(AppsEnum appsEnum) {
        this.appsEnum = appsEnum;
    }

    public AppsEnum getAppsEnum() {
        return appsEnum;
    }
}
