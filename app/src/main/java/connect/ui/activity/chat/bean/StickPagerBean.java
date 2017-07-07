package connect.ui.activity.chat.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * expression of the pager
 * Created by gtq on 2016/12/9.
 */
public class StickPagerBean {
    private boolean bigStick;
    private int bottomPosi;
    private int position;
    private int countCate;
    private String name;
    private List<String> strings = new ArrayList();

    public StickPagerBean(boolean bigStick,int bottom, int position, int countCate,String name, List<String> strings) {
        this.bigStick = bigStick;
        this.bottomPosi=bottom;
        this.position = position;
        this.countCate = countCate;
        this.name=name;
        this.strings = strings;
    }

    public boolean isBigStick() {
        return bigStick;
    }

    public int getBottomPosi() {
        return bottomPosi;
    }

    public int getPosition() {
        return position;
    }

    public int getCountCate() {
        return countCate;
    }

    public String getName() {
        return name;
    }

    public List<String> getStrings() {
        return strings;
    }
}
