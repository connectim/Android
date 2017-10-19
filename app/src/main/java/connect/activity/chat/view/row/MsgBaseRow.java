package connect.activity.chat.view.row;

import android.view.LayoutInflater;

import instant.bean.MsgDirect;
import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;

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
