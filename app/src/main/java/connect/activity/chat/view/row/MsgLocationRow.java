package connect.activity.chat.view.row;

import android.view.LayoutInflater;

import connect.ui.activity.R;
import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.holder.MsgLocationHolder;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgLocationRow extends MsgChatRow {

    @Override
    public MsgBaseHolder buildRowView(LayoutInflater inflater, MsgDirect direct) {
        super.buildRowView(inflater, direct);
        if (direct == MsgDirect.From) {
            container = new BaseContainer(inflater, R.layout.item_chat_location_from);
        } else {
            container = new BaseContainer(inflater, R.layout.item_chat_location_to);
        }
        MsgLocationHolder msgLocationHolder = new MsgLocationHolder(container);
        return msgLocationHolder;
    }
}
