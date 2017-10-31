package connect.activity.chat.view.row;

import android.content.Context;

import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.holder.MsgLuckyHolder;
import connect.ui.activity.R;
import instant.bean.MsgDirect;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgluckRow extends MsgChatRow {

    @Override
    public MsgBaseHolder buildRowView(Context context, MsgDirect direct) {
        super.buildRowView(context, direct);
        if (direct == MsgDirect.From) {
            container = new BaseContainer(context, R.layout.item_chat_luck_from);
        } else {
            container = new BaseContainer(context, R.layout.item_chat_luck_to);
        }
        MsgLuckyHolder msgLuckyHolder = new MsgLuckyHolder(container);
        return msgLuckyHolder;
    }
}
