package connect.activity.chat.view.row;

import android.content.Context;

import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.holder.MsgWebsiteHolder;
import connect.ui.activity.R;
import instant.bean.MsgDirect;

/**
 * Created by pujin on 2017/2/20.
 */
public class MsgWebsiteRow extends MsgChatRow {

    @Override
    public MsgBaseHolder buildRowView(Context context, MsgDirect direct) {
        super.buildRowView(context, direct);
        if (direct == MsgDirect.From) {
            container = new BaseContainer(context, R.layout.item_chat_website_from);
        } else {
            container = new BaseContainer(context, R.layout.item_chat_website_to);
        }
        MsgWebsiteHolder msgWebsiteHolder = new MsgWebsiteHolder(container);
        return msgWebsiteHolder;
    }
}
