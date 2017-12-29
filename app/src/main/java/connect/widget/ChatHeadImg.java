package connect.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.ImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import connect.activity.chat.bean.ContactUpdateBean;
import connect.activity.chat.bean.DestructOpenBean;
import connect.utils.glide.GlideUtil;

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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DestructOpenBean openBean) {
        long time = openBean.getTime();
        if (time <= 0) {
            setVisibility(VISIBLE);
        } else {
            setVisibility(GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContactUpdateBean updateBean) {
        if (updateBean.getContactUid().equals(userUid) && !TextUtils.isEmpty(updateBean.getContactAvatar())) {
            GlideUtil.loadImage(this, updateBean.getContactAvatar());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }
}
