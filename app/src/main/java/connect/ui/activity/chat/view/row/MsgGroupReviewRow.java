package connect.ui.activity.chat.view.row;

import android.view.LayoutInflater;

import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgDirect;
import connect.ui.activity.chat.view.BaseContainer;
import connect.ui.activity.chat.view.holder.MsgBaseHolder;
import connect.ui.activity.chat.view.holder.MsgHandleJoinGroupHolder;

/**
 * Created by pujin on 2017/1/21.
 */

public class MsgGroupReviewRow extends MsgChatRow {

    @Override
    public MsgBaseHolder buildRowView(LayoutInflater inflater, MsgDirect direct) {
        super.buildRowView(inflater, direct);
        container = new BaseContainer(inflater, R.layout.item_chat_groupreview);
        MsgHandleJoinGroupHolder groupHolder = new MsgHandleJoinGroupHolder(container);
        return groupHolder;
    }
}
