package connect.activity.chat.view.row;

import android.content.Context;

import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.holder.MsgImgHolder;
import connect.ui.activity.R;
import instant.bean.MsgDirect;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgImgRow extends MsgChatRow {

    @Override
    public MsgBaseHolder buildRowView(Context context, MsgDirect direct) {
        super.buildRowView(context, direct);
        if (direct == MsgDirect.From) {
            container = new BaseContainer(context, R.layout.item_chat_img_from);
        } else {
            container = new BaseContainer(context, R.layout.item_chat_img_to);
        }
        MsgImgHolder msgImgHolder = new MsgImgHolder(container);
        return msgImgHolder;
    }
}
