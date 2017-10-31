package connect.activity.chat.view.row;

import android.content.Context;

import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.holder.MsgDestructHolder;
import connect.ui.activity.R;
import instant.bean.MsgDirect;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgDestructRow extends MsgBaseRow {

    protected BaseContainer container;

    @Override
    public MsgBaseHolder buildRowView(Context context, MsgDirect direct) {
        super.buildRowView(context, direct);
        container = new BaseContainer(context, R.layout.item_chat_notice);
        MsgDestructHolder msgDestructHolder = new MsgDestructHolder(container);
        return msgDestructHolder;
    }
}
