package connect.activity.chat.view;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import connect.activity.chat.bean.MsgExtEntity;
import connect.ui.activity.R;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.ContainerBean;
import connect.utils.system.SystemUtil;

/**
 * News of outer display (time)
 * Created by gtq on 2016/11/23.
 */
public class BaseContainer extends RelativeLayout {

    public BaseContainer(LayoutInflater layoutInflater, int resource) {
        super(layoutInflater.getContext());
        context = getContext();

        TextView textView = new TextView(getContext(), null);
        textView.setId(R.id.showtime);
        textView.setTextAppearance(layoutInflater.getContext(), R.style.text_chat_msgtime_style);//Bug:can not set Background
        if (Build.VERSION.SDK_INT >= 16) {
            textView.setBackground(getResources().getDrawable(R.drawable.com_notice_r8));
        } else {
            textView.setBackgroundDrawable(getResources().getDrawable(R.drawable.com_notice_r8));
        }

        textView.setGravity(Gravity.CENTER);
        LayoutParams textViewLayoutParams = new LayoutParams(
                SystemUtil.dipToPx(120), SystemUtil.dipToPx(20));
        textViewLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        textViewLayoutParams.setMargins(0, SystemUtil.dipToPx(16), 0, SystemUtil.dipToPx(16));
        addView(textView, textViewLayoutParams);

        View chattingView = layoutInflater.inflate(resource, null);
        int id = chattingView.getId();
        if (id == -1) {
            chattingView.setId(id);
        }
        LayoutParams chattingViewLayoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        chattingViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.showtime);
        chattingViewLayoutParams.setMargins(0, SystemUtil.dipToPx(10), 0, SystemUtil.dipToPx(16));
        addView(chattingView, chattingViewLayoutParams);
    }

    private Context context;
    private MsgExtEntity baseEntity;

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ContainerBean bean) {
        Object[] objects = null;
        if (bean.getObj() != null) {
            objects = (Object[]) bean.getObj();
        }

        if (!((objects[0]).equals(baseEntity.getMessage_id()))) {
            return;
        }

        View view = null;
        switch (bean.getExtType()) {
            case ROBOT_HANDLEAPPLY:
                view = findViewById(R.id.txt3);
                if (view != null) {
                    updateGroupApply((Integer) objects[1], view);
                }
                break;
            case GATHER_DETAIL:
                view = findViewById(R.id.pay);
                if (view != null) {
                    int isSingle = (int) objects[1];

                    if (isSingle == 0) {
                        updateGatherDetail(isSingle, (int) objects[2], 0, view);
                    } else {
                        updateGatherDetail(isSingle, (int) objects[2], (int) objects[3], view);
                    }
                }
                break;
            case TRANSFER_STATE:
                view = findViewById(R.id.txt2);
                if (view != null) {
                    updateTransferState((int) objects[1], view);
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

    public void setBaseEntity(MsgExtEntity baseEntity) {
        this.baseEntity = baseEntity;
    }

    protected void updateGroupApply(int state, View view) {
        String statestr = "";
        switch (state) {
            case 1://agree
                statestr = context.getString(R.string.Chat_Have_agreed);
                break;
            case 2://refuse
                statestr = context.getString(R.string.Chat_Have_refused);
                break;
        }
        ((TextView) view).setText(statestr);
    }

    protected void updateGatherDetail(int issingle, int paycount, int crowdcount, View view) {
        String statestr = "";
        if (issingle == 0) {
            if (paycount == 0) {
            } else if (paycount == 1) {
                statestr = context.getResources().getString(R.string.Wallet_Unconfirmed);
            }
        } else {
            statestr = paycount + "/" + crowdcount + " " + context.getResources().getString(R.string.Chat_Crowd_funding);
        }

        if (!TextUtils.isEmpty(statestr)) {
            ((TextView) view).setText(statestr);
        }
    }

    protected void updateTransferState(int state, View view) {
        String statestr = "";
        switch (state) {
            case 0:
                statestr = context.getString(R.string.Chat_Unpaid);
                break;
            case 1:
                statestr = context.getString(R.string.Wallet_Unconfirmed);
                break;
            case 2:
                statestr = context.getString(R.string.Wallet_Confirmed);
                break;
        }
        ((TextView) view).setText(statestr);
    }
}
