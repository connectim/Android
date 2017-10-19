package connect.activity.chat.view.holder;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import instant.bean.ChatMsgEntity;
import instant.bean.MsgDirect;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.exts.ApplyJoinGroupActivity;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import protos.Connect;

/**
 * Created by pujin on 2017/1/21.
 */

public class MsgInviteGroupHolder extends MsgChatHolder {

    private ImageView cardHead;
    private TextView txt1;
    private TextView txt2;

    private int applyState = 0;

    public MsgInviteGroupHolder(View itemView) {
        super(itemView);
        cardHead = (ImageView) itemView.findViewById(R.id.roundimg1);
        txt1 = (TextView) itemView.findViewById(R.id.txt1);
        txt2 = (TextView) itemView.findViewById(R.id.txt2);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final ChatMsgEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        final Connect.JoinGroupMessage joinGroupMessage = Connect.JoinGroupMessage.parseFrom(msgExtEntity.getContents());

        GlideUtil.loadAvatarRound(cardHead, joinGroupMessage.getAvatar());
        String showTxt = msgExtEntity.parseDirect() == MsgDirect.From ? context.getString(R.string.Link_Invite_you_to_join, joinGroupMessage.getGroupName()) :
                context.getString(R.string.Link_Invite_friend_to_join, joinGroupMessage.getGroupName());
        txt1.setText(showTxt);

        applyState = ParamManager.getInstance().loadGroupApplyMember(joinGroupMessage.getGroupId(), msgExtEntity.getMessage_id());
        String statestr = "";
        switch (applyState) {
            case 0://Has not to apply
                break;
            case 1:
                statestr = context.getString(R.string.Chat_Have_agreed);
                break;
            case 2:
                statestr = context.getString(R.string.Chat_Have_refused);
                break;
        }
        txt2.setText(statestr);
        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (msgExtEntity.parseDirect() == MsgDirect.From) {
                    if (applyState == 0) {
                        ApplyJoinGroupActivity.startActivity((Activity) context, ApplyJoinGroupActivity.EApplyGroup.GROUPKEY, joinGroupMessage.getGroupId(), getMsgExtEntity().getMessage_ower(), joinGroupMessage.getToken());
                    }
                }
            }
        });
    }
}
