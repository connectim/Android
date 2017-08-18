package connect.activity.chat.view.row;

import android.view.LayoutInflater;

import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.holder.MsgEncryptHolder;
import connect.ui.activity.R;

/**
 * Created by Administrator on 2017/8/16.
 */
public class MsgEncryptRow extends MsgBaseRow {
    protected BaseContainer container;

    @Override
    public MsgBaseHolder buildRowView(LayoutInflater inflater, MsgDirect direct) {
        super.buildRowView(inflater, direct);
        container = new BaseContainer(inflater, R.layout.item_chat_destruct);
        MsgEncryptHolder encryptHolder = new MsgEncryptHolder(container);
        return encryptHolder;
    }
}
