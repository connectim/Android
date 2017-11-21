package connect.activity.chat.view.row;

import android.content.Context;

import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.holder.MsgSysAdHolder;
import connect.ui.activity.R;
import instant.bean.MsgDirect;

/**
 * Created by puin on 17-11-22.
 */

public class MsgSubscribeRow extends MsgBaseRow {

    @Override
    public MsgBaseHolder buildRowView(Context context, MsgDirect direct) {
        super.buildRowView(context, direct);
        container = new BaseContainer(context, R.layout.item_chat_sysad);
        MsgSysAdHolder msgAdHolder = new MsgSysAdHolder(container);
        return msgAdHolder;
    }
}
