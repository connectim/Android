package connect.activity.chat.view.row;

import android.content.Context;

import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.holder.MsgInviteGroupHolder;
import connect.ui.activity.R;
import instant.bean.MsgDirect;

/**
 * Created by pujin on 2017/1/21.
 */

public class MsgInviteGroupRow extends MsgChatRow {

    @Override
    public MsgBaseHolder buildRowView(Context context, MsgDirect direct) {
        super.buildRowView(context, direct);
        if (direct == MsgDirect.From) {
            container = new BaseContainer(context, R.layout.item_chat_invitegroup_from);
        } else {
            container = new BaseContainer(context, R.layout.item_chat_invitegroup_to);
        }
        MsgInviteGroupHolder msgInviteGroupHolder = new MsgInviteGroupHolder(container);
        return msgInviteGroupHolder;
    }
}