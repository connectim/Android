package connect.activity.chat.view.holder;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import instant.bean.ChatMsgEntity;
import instant.bean.MsgDirect;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.ContactEntity;
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
    public void buildRowData(MsgBaseHolder msgBaseHolder, ChatMsgEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        Connect.DestructMessage destructMessage = Connect.DestructMessage.parseFrom(msgExtEntity.getContents());

        String name = "";
        if (msgExtEntity.parseDirect() == MsgDirect.From) {
            String publicKey = msgExtEntity.getMessage_from();
            ContactEntity contactEntity = ContactHelper.getInstance().loadFriendEntity(publicKey);
            name = TextUtils.isEmpty(contactEntity.getRemark()) ? contactEntity.getUsername() : contactEntity.getRemark();
        } else {
            name = context.getResources().getString(R.string.Chat_You);
        }

        String content = "";
        if (destructMessage.getTime() <= 0) {
            content = context.getResources().getString(R.string.Chat_disable_the_self_descruct, name);
        } else {
            content = context.getResources().getString(R.string.Chat_set_the_self_destruct_timer_to, name, TimeUtil.parseBurnTime(destructMessage.getTime()));
        }
        notifyTxt.setText(content);
    }
}
