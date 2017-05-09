package connect.ui.activity.chat.view.row;

import android.view.LayoutInflater;

import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgDirect;
import connect.ui.activity.chat.view.BaseContainer;
import connect.ui.activity.chat.view.holder.MsgBaseHolder;
import connect.ui.activity.chat.view.holder.MsgTransferHolder;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgTransfer extends MsgChatRow{

    @Override
    public MsgBaseHolder buildRowView(LayoutInflater inflater, MsgDirect direct) {
        super.buildRowView(inflater, direct);
        if (direct == MsgDirect.From) {
            container = new BaseContainer(inflater, R.layout.item_chat_transfer_from);
        } else {
            container = new BaseContainer(inflater, R.layout.item_chat_transfer_to);
        }
        MsgTransferHolder msgTransferHolder = new MsgTransferHolder(container);
        return msgTransferHolder;
    }
}
