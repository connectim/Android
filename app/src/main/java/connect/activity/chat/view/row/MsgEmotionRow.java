package connect.activity.chat.view.row;

import android.content.Context;

import connect.activity.chat.view.BaseContainer;
import connect.activity.chat.view.holder.MsgBaseHolder;
import connect.activity.chat.view.holder.MsgEmotionHolder;
import connect.ui.activity.R;
import instant.bean.MsgDirect;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgEmotionRow extends MsgChatRow {

    @Override
    public MsgBaseHolder buildRowView(Context context, MsgDirect direct) {
        super.buildRowView(context, direct);
        if (direct == MsgDirect.From) {
            container = new BaseContainer(context, R.layout.item_chat_emotion_from);
        } else {
            container = new BaseContainer(context, R.layout.item_chat_emotion_to);
        }
        MsgEmotionHolder msgEmotionHolder = new MsgEmotionHolder(container);
        return msgEmotionHolder;
    }
}
