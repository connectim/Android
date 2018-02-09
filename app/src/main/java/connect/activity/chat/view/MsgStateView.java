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

import connect.activity.chat.bean.RecExtBean;
import connect.ui.activity.R;
import instant.bean.ChatMsgEntity;

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
    private ChatMsgEntity msgExtEntity;

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
            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.RESEND, msgExtEntity);
        }
    };

    public void setMsgEntity(ChatMsgEntity entity) {
        this.msgExtEntity = entity;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN://Click on the button, failure Delay sending failed messages
                if (msgExtEntity.getSend_status() == 2) {
                    handler.sendEmptyMessageDelayed(MESSAGE_WHAT, MESSAGE_DELAY);
                    updateMsgState(0);
                    msgExtEntity.setSend_status(0);
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
                msgExtEntity.setSend_status(0);
                break;
            case 1:
                setVisibility(GONE);
                msgExtEntity.setSend_status(1);
                break;
            case 2:
                setVisibility(VISIBLE);
                addView(createFailImag());
                msgExtEntity.setSend_status(2);
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
                if (msgExtEntity.getMessage_id().equals(msgid)) {
                    if (state != 1) {
                        updateMsgState(state);
                    }
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