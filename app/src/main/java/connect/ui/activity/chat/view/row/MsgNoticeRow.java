package connect.ui.activity.chat.view.row;

import android.view.LayoutInflater;

import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgDirect;
import connect.ui.activity.chat.view.BaseContainer;
import connect.ui.activity.chat.view.holder.MsgBaseHolder;
import connect.ui.activity.chat.view.holder.MsgNoticeHolder;

/**
 * Created by gtq on 2016/12/19.
 */
public class MsgNoticeRow extends MsgBaseRow {

    @Override
    public MsgBaseHolder buildRowView(LayoutInflater inflater, MsgDirect direct) {
        super.buildRowView(inflater, direct);
        container = new BaseContainer(inflater, R.layout.item_chat_notice);
        MsgNoticeHolder msgNoticeHolder = new MsgNoticeHolder(container);
        return msgNoticeHolder;
    }
}