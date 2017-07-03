package connect.ui.activity.chat.view.row;

import android.view.LayoutInflater;

import connect.ui.activity.chat.bean.MsgDirect;
import connect.ui.activity.chat.view.BaseContainer;
import connect.ui.activity.chat.view.holder.MsgBaseHolder;

/**
 * Created by gtq on 2016/11/23.
 */
public abstract class MsgBaseRow implements MsgRowInter {
    private LayoutInflater inflater;
    private MsgDirect direct;
    protected BaseContainer container;

    @Override
    public MsgBaseHolder buildRowView(LayoutInflater inflater, MsgDirect direct) {
        this.inflater = inflater;
        this.direct = direct;
        return null;
    }
}
