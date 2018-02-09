package connect.activity.chat.view.row;

import android.content.Context;
import android.view.LayoutInflater;

import connect.ui.activity.R;
import instant.bean.MsgDirect;
import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.holder.MsgVideoHolder;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgVideoRow extends MsgChatRow{

    @Override
    public MsgBaseHolder buildRowView(Context context, MsgDirect direct) {
        super.buildRowView(context, direct);
        if (direct == MsgDirect.From) {
            container = new BaseContainer(context, R.layout.item_chat_video_from);
        } else {
            container = new BaseContainer(context, R.layout.item_chat_video_to);
        }
        MsgVideoHolder msgVideoHolder = new MsgVideoHolder(container);
        return msgVideoHolder;
    }
}
