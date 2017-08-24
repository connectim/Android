package connect.widget;

import android.content.Context;
import android.util.AttributeSet;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import connect.activity.chat.bean.DestructOpenBean;
import connect.widget.roundedimageview.RoundedImageView;

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
    public void onEventMainThread(DestructOpenBean openBean) {
        long time = openBean.getTime();
        if (time <= 0) {
            setVisibility(VISIBLE);
        } else {
            setVisibility(GONE);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }
}
