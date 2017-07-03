package connect.widget;

import android.content.Context;
import android.util.AttributeSet;

import connect.activity.chat.bean.BurnNotice;
import connect.widget.roundedimageview.RoundedImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by gtq on 2016/12/13.
 */
public class ChatHeadImg extends RoundedImageView {

    public ChatHeadImg(Context context) {
        super(context);
    }

    public ChatHeadImg(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatHeadImg(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BurnNotice notice) {
        Object[] objects = (Object[]) notice.getObjs();
        if (notice.getBurnType() == BurnNotice.BurnType.BURN_START) {
            long burntime = (Long) objects[0];
            if (burntime == 0) {
                setVisibility(VISIBLE);
            } else {
                setVisibility(GONE);
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }
}
