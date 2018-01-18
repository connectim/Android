package connect.activity.chat.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.bumptech.glide.Glide;

import connect.ui.activity.R;
import connect.utils.FileUtil;
import connect.utils.TimeUtil;
import connect.utils.log.LogManager;
import connect.utils.system.SystemDataUtil;
import connect.utils.system.SystemUtil;

/**
 * Created by gtq on 2016/12/10.
 */
public class PopWindowImg extends ImageView {

    private String Tag = "PopupImg";
    private String filePath = "";

    public PopWindowImg(Context context) {
        super(context);
    }

    public PopWindowImg(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PopWindowImg(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private final int LONG_PRESS = 100;
    private final long LONG_PRESS_TIME = 500;

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LONG_PRESS:
                    touchLong();
                    break;
            }
        }
    };

    public void setGifPath(String path) {
        this.filePath = path;
    }

    private float actionX;
    private float actionY;
    private long downtime = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                actionX = event.getX();
                actionY = event.getY();
                downtime = TimeUtil.getCurrentTimeInLong();

                handler.sendEmptyMessageDelayed(LONG_PRESS, LONG_PRESS_TIME);
                break;
            case MotionEvent.ACTION_UP:
                if (TimeUtil.getCurrentTimeInLong() - downtime < 200 && (event.getX() - actionX < 20) && (event.getY() - actionY < 20)) {
                    filePath= FileUtil.realFileName(filePath);
                    popListener.OnClick(filePath);
                }
            case MotionEvent.ACTION_CANCEL:
                handler.removeMessages(LONG_PRESS);
                touchCancel();
                break;
        }
        return true;
    }

    private PopupWindow window = null;

    private void touchLong() {
        LogManager.getLogger().d(Tag, "touchLong");

        View gifView = LayoutInflater.from(getContext()).inflate(R.layout.view_popupimg, null);
        ImageView imageView = (ImageView) gifView.findViewById(R.id.gif);
        Glide.with(getContext())
                .load(filePath)
                .into(imageView);

        int[] location = SystemUtil.locationOnScreen(this);
        window = new PopupWindow(gifView, SystemUtil.dipToPx(100), SystemUtil.dipToPx(100));
        window.setAnimationStyle(R.style.Dialog);
        window.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_trans_while));
        window.setFocusable(true);
        window.setOutsideTouchable(true);
        window.update();

        //location[1] = location[1] - getHeight();

        if ((location[0] - SystemUtil.dipToPx(25) - SystemUtil.dipToPx(10)) <= SystemUtil.dipToPx(10)) {
            location[0] = SystemUtil.dipToPx(10);
        } else if ((location[0] - SystemUtil.dipToPx(25) + SystemUtil.dipToPx(100) + SystemUtil.dipToPx(20)) >= SystemDataUtil.getScreenWidth()) {
            location[0] = SystemDataUtil.getScreenWidth() - SystemUtil.dipToPx(10) - SystemUtil.dipToPx(100);
        } else {
            location[0] = location[0] - SystemUtil.dipToPx(25);
        }
        window.showAtLocation(this, Gravity.TOP | Gravity.START, location[0], location[1] - SystemUtil.dipToPx(100));
    }

    private void touchCancel() {
        LogManager.getLogger().d(Tag, "touchCancel");
        if (window != null) {
            if (window.isShowing()) {
                window.dismiss();
            }
            window = null;
        }
    }

    private IPopWindowListener popListener;

    public interface IPopWindowListener {
        void OnClick(String filePath);
    }

    public void setPopListener(IPopWindowListener popListener) {
        this.popListener = popListener;
    }
}
