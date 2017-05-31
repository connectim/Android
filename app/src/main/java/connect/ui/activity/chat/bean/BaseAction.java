package connect.ui.activity.chat.bean;

import java.io.Serializable;

/**
 * Created by gtq on 2016/11/24.
 */
public class BaseAction implements Serializable{
    private int iconResId;
    private int titleId;

    public BaseAction(int iconResId) {
        this.iconResId = iconResId;
    }

    public BaseAction(int iconResId, int titleId) {
        this.iconResId = iconResId;
        this.titleId = titleId;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public int getTitleId() {
        return titleId;
    }

    public void setTitleId(int titleId) {
        this.titleId = titleId;
    }
}