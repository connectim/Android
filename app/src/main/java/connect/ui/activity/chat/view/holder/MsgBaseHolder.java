package connect.ui.activity.chat.view.holder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgEntity;
import connect.ui.activity.chat.view.BaseContainer;
import connect.utils.TimeUtil;

/**
 * Handle all message common state (click/delivery status)
 * Created by gtq on 2016/11/23.
 */
public abstract class MsgBaseHolder extends RecyclerView.ViewHolder {
    protected Context context;
    protected BaseContainer baseContainer;

    private TextView timeTxt;

    /** The message time interval */
    private long MSG_TIMESPACE = 3 * 1000 * 60;

    public MsgBaseHolder(View itemView) {
        super(itemView);
        baseContainer= (BaseContainer) itemView;
        context = itemView.getContext();
        timeTxt = (TextView) itemView.findViewById(R.id.showtime);
    }

    public void buildRowData(MsgBaseHolder msgBaseHolder, MsgEntity baseEntity) {
        baseContainer.setBaseEntity(baseEntity);
    }

    /**
     * Message display rules of time According to the current message time and interval between the time of the next message
     * If this is the last message, but also need to a message on its time to calculate
     *
     * @param curtime
     * @param nexttime The next message on time or a time
     */
    public void buildMsgTime(long curtime, long nexttime) {
        String showtime = "";
        try {
            if (Math.abs(curtime - nexttime) > MSG_TIMESPACE) {
                showtime = TimeUtil.getMsgTime(curtime, nexttime);
            }
            showTime(showtime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * show time
     *
     * @param show
     */
    public void showTime(String show) {
        if (TextUtils.isEmpty(show)) {
            timeTxt.setVisibility(View.GONE);
        } else {
            timeTxt.setVisibility(View.VISIBLE);
            timeTxt.setText(show);
        }
    }

    /**
     * long pression notice
     *
     * @return
     */
    public String[] longPressPrompt() {
        return context.getResources().getStringArray(R.array.prompt_delete);
    }
}
