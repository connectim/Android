package connect.activity.set.bean;

import connect.database.green.DaoHelper.ParamManager;

/**
 * system setting
 */

public class SystemSetBean {

    private String language; // 系统语言
    private boolean ring; // 系统是否开启铃声
    private boolean vibrate; // 系统是否开启震动

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isRing() {
        return ring;
    }

    public void setRing(boolean ring) {
        this.ring = ring;
    }

    public boolean isVibrate() {
        return vibrate;
    }

    public void setVibrate(boolean vibrate) {
        this.vibrate = vibrate;
    }

    public static SystemSetBean initSystemSet(){
        SystemSetBean systemSetBean = new SystemSetBean();
        systemSetBean.setLanguage("");
        systemSetBean.setVibrate(true);
        systemSetBean.setRing(true);
        ParamManager.getInstance().putSystemSet(systemSetBean);
        return systemSetBean;
    }

    public static SystemSetBean putLanguage(String language){
        SystemSetBean systemSetBean = ParamManager.getInstance().getSystemSet();
        systemSetBean.setLanguage(language);
        ParamManager.getInstance().putSystemSet(systemSetBean);
        return systemSetBean;
    }

    public static SystemSetBean putRing(boolean ring){
        SystemSetBean systemSetBean = ParamManager.getInstance().getSystemSet();
        systemSetBean.setRing(ring);
        ParamManager.getInstance().putSystemSet(systemSetBean);
        return systemSetBean;
    }

    public static SystemSetBean putVibrate(boolean vibrate){
        SystemSetBean systemSetBean = ParamManager.getInstance().getSystemSet();
        systemSetBean.setVibrate(vibrate);
        ParamManager.getInstance().putSystemSet(systemSetBean);
        return systemSetBean;
    }

}
