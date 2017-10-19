package connect.activity.chat.view.row;

import android.view.LayoutInflater;

import connect.ui.activity.R;
import instant.bean.MsgDirect;
import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgSysAdHolder;
import connect.activity.chat.view.holder.MsgBaseHolder;

/**
 * Created by pujin on 2017/3/24.
 */
public class MsgSysAdRow extends MsgBaseRow {

    @Override
    public MsgBaseHolder buildRowView(LayoutInflater inflater, MsgDirect direct) {
        super.buildRowView(inflater, direct);
        container = new BaseContainer(inflater, R.layout.item_chat_sysad);
        MsgSysAdHolder msgAdHolder = new MsgSysAdHolder(container);
        return msgAdHolder;
    }
}
