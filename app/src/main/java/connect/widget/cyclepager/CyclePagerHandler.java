package connect.widget.cyclepager;

import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;

/**
 * 滚动控制Handle
 */
public class CyclePagerHandler extends Handler {

    private ViewPager vp;
    /** 是否开启轮滚 */
    private boolean isStart = true;
    /** 轮滚Message code */
    public static final int CODE = 1;
    /** 轮滚间隔时间 */
    public static final int CYCLE_TIME = 3000;

    public CyclePagerHandler(ViewPager vp) {
        this.vp = vp;
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case CODE:
                if(vp != null && isStart){
                    int po = vp.getCurrentItem();
                    vp.setCurrentItem(++po);
                    this.sendEmptyMessageDelayed(CODE, CYCLE_TIME);
                }
                break;
            default:
                break;
        }
    }

    public void setStart(boolean isStart) {
        this.isStart = isStart;
    }
}
