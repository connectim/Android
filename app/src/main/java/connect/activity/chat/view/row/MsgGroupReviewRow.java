package connect.activity.chat.view.row;

import android.content.Context;

import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.holder.MsgHandleJoinGroupHolder;
import connect.ui.activity.R;
import instant.bean.MsgDirect;

/**
 * Created by pujin on 2017/1/21.
 */

public class MsgGroupReviewRow extends MsgChatRow {

    @Override
    public MsgBaseHolder buildRowView(Context context, MsgDirect direct) {
        super.buildRowView(context, direct);
        container = new BaseContainer(context, R.layout.item_chat_groupreview);
        MsgHandleJoinGroupHolder groupHolder = new MsgHandleJoinGroupHolder(container);
        return groupHolder;
    }
}
