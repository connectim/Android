package connect.ui.activity.home.bean;

/**
 * Created by Administrator on 2016/12/10.
 */
public class WalletMenuBean {

    private int iconID;
    private int nameID;

    public WalletMenuBean(int iconID, int nameID) {
        this.iconID = iconID;
        this.nameID = nameID;
    }

    public int getIconID() {
        return iconID;
    }

    public void setIconID(int iconID) {
        this.iconID = iconID;
    }

    public int getNameID() {
        return nameID;
    }

    public void setNameID(int nameID) {
        this.nameID = nameID;
    }
}
