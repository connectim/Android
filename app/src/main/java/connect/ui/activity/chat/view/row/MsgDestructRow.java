package connect.ui.activity.chat.view.row;

import android.view.LayoutInflater;

import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgDirect;
import connect.ui.activity.chat.view.BaseContainer;
import connect.ui.activity.chat.view.holder.MsgBaseHolder;
import connect.ui.activity.chat.view.holder.MsgDestructHolder;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgDestructRow extends MsgBaseRow {
    protected BaseContainer container;

    @Override
    public MsgBaseHolder buildRowView(LayoutInflater inflater, MsgDirect direct) {
        super.buildRowView(inflater, direct);
        container = new BaseContainer(inflater, R.layout.item_chat_destruct);
        MsgDestructHolder msgDestructHolder = new MsgDestructHolder(container);
        return msgDestructHolder;
    }
}
