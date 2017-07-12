package connect.activity.chat.view.row;

import android.view.LayoutInflater;

import connect.ui.activity.R;
import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.holder.MsgImgHolder;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgImgRow extends MsgChatRow {

    @Override
    public MsgBaseHolder buildRowView(LayoutInflater inflater, MsgDirect direct) {
        super.buildRowView(inflater, direct);
        if (direct == MsgDirect.From) {
            container = new BaseContainer(inflater, R.layout.item_chat_img_from);
        } else {
            container = new BaseContainer(inflater, R.layout.item_chat_img_to);

        }
        MsgImgHolder msgImgHolder = new MsgImgHolder(container);
        return msgImgHolder;
    }
}
