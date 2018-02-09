package connect.activity.chat.view.row;

import android.content.Context;
import android.view.LayoutInflater;

import instant.bean.MsgDirect;
import connect.activity.chat.view.holder.MsgBaseHolder;

/**
 * Created by gtq on 2016/11/23.
 */
public interface MsgRowInter {

    /** From:0  To:1 */
    MsgBaseHolder buildRowView(Context context, MsgDirect direct);
}
