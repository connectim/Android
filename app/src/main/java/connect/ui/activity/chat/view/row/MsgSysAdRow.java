package connect.ui.activity.chat.view.row;

import android.view.LayoutInflater;

import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgDirect;
import connect.ui.activity.chat.view.BaseContainer;
import connect.ui.activity.chat.view.holder.MsgSysAdHolder;
import connect.ui.activity.chat.view.holder.MsgBaseHolder;

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
