package connect.activity.chat.view.holder;

import android.view.View;
import android.widget.TextView;

import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.bean.RoomSession;
import connect.ui.activity.R;
import connect.utils.TimeUtil;
import protos.Connect;

/**
 * Created by gtq on 2016/11/23.
 */
public class MsgDestructHolder extends MsgBaseHolder{

    private TextView notifyTxt;

    public MsgDestructHolder(View itemView) {
        super(itemView);
        notifyTxt = (TextView) itemView.findViewById(R.id.notify);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, MsgExtEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        Connect.DestructMessage destructMessage = Connect.DestructMessage.parseFrom(msgExtEntity.getContents());

        String content = "";
        String name = msgExtEntity.parseDirect() == MsgDirect.From ? RoomSession.getInstance().getUserInfo().getUsername() :
                context.getResources().getString(R.string.Chat_You);
        if (destructMessage.getTime() == 0) {
            content = context.getResources().getString(R.string.Chat_disable_the_self_descruct, name);
        } else {
            content = context.getResources().getString(R.string.Chat_set_the_self_destruct_timer_to, name, TimeUtil.parseBurnTime(destructMessage.getTime()));
        }
        notifyTxt.setText(content);
    }
}
