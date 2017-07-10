package connect.activity.chat.view;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import connect.ui.activity.R;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.RecExtBean;

/**
 * State news component
 * Send the progress bar sent successfully hide send button click resend display failure
 * Created by gtq on 2016/12/30.
 */
public class MsgStateView extends RelativeLayout {

    private static final int MESSAGE_WHAT = 50;
    /** The message delay processing */
    private static final long MESSAGE_DELAY = 1000;
    /** Send the failure message */
    private MsgEntity chatBean;

    public MsgStateView(Context context) {
        super(context);
    }

    public MsgStateView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MsgStateView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private Handler handler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            RecExtBean.sendRecExtMsg(RecExtBean.ExtType.RESEND, chatBean);
        }
    };

    public void setMsgEntity(MsgEntity bean) {
        this.chatBean = bean;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://Click on the button, failure Delay sending failed messages
                if (chatBean.getSendstate() == 2) {
                    handler.sendEmptyMessageDelayed(MESSAGE_WHAT, MESSAGE_DELAY);
                    updateMsgState(0);
                    chatBean.setSendstate(0);
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    public void updateMsgState(int state){
        removeAllViews();
        switch (state){
            case 0:
                setVisibility(VISIBLE);
                addView(createNewBar());
                chatBean.setSendstate(0);
                break;
            case 1:
                setVisibility(GONE);
                chatBean.setSendstate(1);
                break;
            case 2:
                setVisibility(VISIBLE);
                addView(createFailImag());
                chatBean.setSendstate(2);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RecExtBean bean) {
        Object[] objects = null;
        if (bean.getObj() != null) {
            objects = (Object[]) bean.getObj();
        }

        switch (bean.getExtType()) {
            case MSGSTATEVIEW:
                 String msgid = (String) objects[0];
                int state = (int) objects[1];
                if (chatBean.getMsgDefinBean().getMessage_id().equals(msgid)) {
                    updateMsgState(state);
                }
                break;
        }
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

    public ProgressBar createNewBar() {
        ProgressBar bar = new ProgressBar(getContext());
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getLayoutParams();
        bar.setLayoutParams(params);
        return bar;
    }

    public ImageView createFailImag() {
        ImageView image = new ImageView(getContext());
        image.setImageResource(R.mipmap.attention_message2x);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) getLayoutParams();
        image.setLayoutParams(params);
        return image;
    }
}