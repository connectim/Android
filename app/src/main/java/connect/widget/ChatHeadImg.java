package connect.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;

public class ChatHeadImg extends ImageView {

    private String userUid = "";

    public ChatHeadImg(Context context) {
        super(context);
    }

    public ChatHeadImg(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatHeadImg(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setUserUid(String uid) {
        this.userUid = uid;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }
}
