package connect.activity.chat.view.row;

import android.view.LayoutInflater;

import connect.ui.activity.R;
import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.holder.MsgVoiceHolder;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgVoiceRow extends MsgChatRow {

    @Override
    public MsgBaseHolder buildRowView(LayoutInflater inflater, MsgDirect direct) {
        super.buildRowView(inflater, direct);
        if (direct == MsgDirect.From) {
            container = new BaseContainer(inflater, R.layout.item_chat_voice_from);
        } else {
            container = new BaseContainer(inflater, R.layout.item_chat_voice_to);
        }
        MsgVoiceHolder msgVoiceHolder = new MsgVoiceHolder(container);
        return msgVoiceHolder;
    }
}
