package connect.activity.chat.view.row;

import android.content.Context;

import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.holder.MsgWarehouseHolder;
import connect.ui.activity.R;
import instant.bean.MsgDirect;

/**
 * Created by PuJin on 2018/2/7.
 */

public class MsgWarehouseRow extends MsgBaseRow {

    @Override
    public MsgBaseHolder buildRowView(Context context, MsgDirect direct) {
        super.buildRowView(context, direct);
        container = new BaseContainer(context, R.layout.item_chat_warehouse);
        MsgWarehouseHolder warehouseHolder = new MsgWarehouseHolder(container);
        return warehouseHolder;
    }
}
