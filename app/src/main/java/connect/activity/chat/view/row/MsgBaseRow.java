package connect.activity.chat.view.row;

import android.content.Context;

import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import instant.bean.MsgDirect;

/**
 * Created by gtq on 2016/11/23.
 */
public abstract class MsgBaseRow implements MsgRowInter {

    private Context context;
    private MsgDirect direct;
    protected BaseContainer container;

    @Override
    public MsgBaseHolder buildRowView(Context context, MsgDirect direct) {
        this.context = context;
        this.direct = direct;
        return null;
    }
}
