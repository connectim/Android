package connect.ui.activity.chat.view.row;

import android.view.LayoutInflater;

import connect.ui.activity.R;
import connect.ui.activity.chat.bean.MsgDirect;
import connect.ui.activity.chat.view.BaseContainer;
import connect.ui.activity.chat.view.holder.MsgBaseHolder;
import connect.ui.activity.chat.view.holder.MsgInviteGroupHolder;

/**
 * Created by pujin on 2017/1/21.
 */

public class MsgInviteGroupRow extends MsgChatRow {

    @Override
    public MsgBaseHolder buildRowView(LayoutInflater inflater, MsgDirect direct) {
        super.buildRowView(inflater, direct);
        if (direct == MsgDirect.From) {
            container = new BaseContainer(inflater, R.layout.item_chat_invitegroup_from);
        } else {
            container = new BaseContainer(inflater, R.layout.item_chat_invitegroup_to);
        }
        MsgInviteGroupHolder msgInviteGroupHolder = new MsgInviteGroupHolder(container);
        return msgInviteGroupHolder;
    }
}