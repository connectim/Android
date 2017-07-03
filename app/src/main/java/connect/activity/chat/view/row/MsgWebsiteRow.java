package connect.activity.chat.view.row;

import android.view.LayoutInflater;

import connect.ui.activity.R;
import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.holder.MsgWebsiteHolder;

/**
 * Created by pujin on 2017/2/20.
 */
public class MsgWebsiteRow extends MsgChatRow {

    @Override
    public MsgBaseHolder buildRowView(LayoutInflater inflater, MsgDirect direct) {
        super.buildRowView(inflater, direct);
        if (direct == MsgDirect.From) {
            container = new BaseContainer(inflater, R.layout.item_chat_website_from);
        } else {
            container = new BaseContainer(inflater, R.layout.item_chat_website_to);
        }
        MsgWebsiteHolder msgWebsiteHolder = new MsgWebsiteHolder(container);
        return msgWebsiteHolder;
    }
}
