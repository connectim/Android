package connect.activity.chat.view.row;

import android.content.Context;

import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.holder.MsgEncryptHolder;
import connect.ui.activity.R;
import instant.bean.MsgDirect;

/**
 * Created by Administrator on 2017/8/16.
 */
public class MsgEncryptRow extends MsgBaseRow {
    protected BaseContainer container;

    @Override
    public MsgBaseHolder buildRowView(Context context, MsgDirect direct) {
        super.buildRowView(context, direct);
        container = new BaseContainer(context, R.layout.item_chat_destruct);
        MsgEncryptHolder encryptHolder = new MsgEncryptHolder(container);
        return encryptHolder;
    }
}
