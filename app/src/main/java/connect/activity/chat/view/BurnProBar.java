package connect.activity.chat.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.view.View;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import connect.activity.chat.bean.DestructReadBean;
import connect.activity.chat.bean.LinkMessageRow;
import instant.bean.ChatMsgEntity;
import instant.bean.MsgDirect;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.bean.MsgSend;
import connect.activity.chat.bean.RecExtBean;
import connect.database.green.DaoHelper.MessageHelper;
import connect.ui.activity.R;
import connect.utils.TimeUtil;
import connect.utils.system.SystemUtil;

/**
 * Created by gtq on 2016/12/13.
 */
public class BurnProBar extends View {

    private RectF rectf;
    private int ROUND_DIRECT = -360;
    private float value = -360;
    private Paint paint;

    private BurnCountTimer burnTimer = null;
    private ChatMsgEntity msgExtEntity;

    public BurnProBar(Context context) {
        super(context);
        initView();
    }

    public BurnProBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public BurnProBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void initView() {
        int dp = SystemUtil.dipToPx(20);
        rectf = new RectF(5, 5, dp - 5, dp - 5);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);
    }

    public void setValue(int value) {
        this.value = value;
        invalidate();
    }

    public void setMsgExtEntity(ChatMsgEntity msgExtEntity) {
        this.msgExtEntity = msgExtEntity;
    }

    public void loadBurnMsg() {
        setValue(ROUND_DIRECT);
        cancelTimer();

        long burnstart = msgExtEntity.getSnap_time();
        if (burnstart > 0) {
            startBurnRead();
        }
    }

    public void startBurnRead() {
        long burnstart = msgExtEntity.getSnap_time();
        if (burnstart <= 0) {
            burnstart = TimeUtil.getCurrentTimeInLong();
            msgExtEntity.setSnap_time(burnstart);
            MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
        }

        long remainTime = msgExtEntity.parseDestructTime() - (TimeUtil.getCurrentTimeInLong() - burnstart);
        burnTimer = new BurnCountTimer(remainTime, 500);
        burnTimer.start();

        MsgDirect direct = msgExtEntity.parseDirect();
        if (direct == MsgDirect.From) {
            MsgSend.sendOuterMsg(LinkMessageRow.Self_destruct_Receipt, msgExtEntity.getMessage_id());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DestructReadBean readBean) {
        if (msgExtEntity.getMessage_id().equals(readBean.getMessageId())) {
            startBurnRead();
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RecExtBean bean) {
        if (msgExtEntity == null) {//group message haven't destruct message
            return;
        }

        Object[] objects = null;
        if (bean.getObj() != null) {
            objects = (Object[]) bean.getObj();
        }

        switch (bean.getExtType()) {
            case MSGSTATEVIEW:
                String msgid = (String) objects[0];
                int state = (int) objects[1];
                String curMsgid = msgExtEntity.getMessage_id();
                if (msgid.equals(curMsgid)) {
                    switch (state) {
                        case 0://sending
                            setVisibility(GONE);
                            break;
                        case 1://send success
                            setVisibility(VISIBLE);
                            break;
                        case 2://send fail
                            setVisibility(GONE);
                            break;
                    }
                }
                break;
        }
    }

    protected void cancelTimer() {
        if (burnTimer != null) {
            burnTimer.cancel();
            burnTimer = null;
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

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setColor(getResources().getColor(R.color.color_adb3bc));
        canvas.drawArc(rectf, -90, 360, false, paint);
        paint.setColor(getResources().getColor(R.color.color_00c400));
        canvas.drawArc(rectf, -90, value, false, paint);
    }

    private class BurnCountTimer extends CountDownTimer {

        public BurnCountTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            value = 0;
            invalidate();

            RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.DELMSG, msgExtEntity);
            MessageHelper.getInstance().deleteMsgByid(msgExtEntity.getMessage_id());
        }

        @Override
        public void onTick(long millisUntilFinished) {
            value = millisUntilFinished * ROUND_DIRECT / msgExtEntity.parseDestructTime();
            invalidate();
        }
    }
}
