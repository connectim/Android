package connect.activity.chat.view.holder;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import connect.activity.chat.bean.ApplyGroupBean;
import connect.activity.chat.bean.MsgExtEntity;
import connect.activity.chat.exts.HandleGroupRequestActivity;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.utils.glide.GlideUtil;
import instant.bean.ChatMsgEntity;
import protos.Connect;

/**
 * Created by pujin on 2017/1/21.
 */

public class MsgHandleJoinGroupHolder extends MsgChatHolder {

    private ImageView roundedImageView;
    private TextView txt1;
    private TextView txt3;

    private String groupApplyKey = null;
    private ApplyGroupBean applyGroupBean;

    public MsgHandleJoinGroupHolder(View itemView) {
        super(itemView);
        roundedImageView = (ImageView) itemView.findViewById(R.id.roundimg1);
        txt1 = (TextView) itemView.findViewById(R.id.txt1);
        txt3 = (TextView) itemView.findViewById(R.id.txt3);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final ChatMsgEntity msgExtEntity) throws Exception {
        super.buildRowData(msgBaseHolder, msgExtEntity);
        Connect.Reviewed reviewed = Connect.Reviewed.parseFrom(msgExtEntity.getContents());

        Connect.UserInfo userInfo = reviewed.getUserInfo();

        GlideUtil.loadAvatarRound(roundedImageView, userInfo.getAvatar());
        txt1.setText(context.getString(R.string.Link_apply_to_join_group_chat, userInfo.getUsername(), reviewed.getName()));

        groupApplyKey = reviewed.getIdentifier() + userInfo.getPubKey();
        applyGroupBean = ParamManager.getInstance().loadGroupApply(groupApplyKey);
        String statestr = "";
        switch (applyGroupBean.getState()) {
            case 0://first apply
                statestr = "";
                break;
            case -1://new apply
                statestr = context.getString(R.string.Chat_New_application);
                break;
            case 1:
                statestr = context.getString(R.string.Chat_Have_agreed);
                break;
            case 2:
                statestr = context.getString(R.string.Chat_Have_refused);
                break;
        }
        txt3.setText(statestr);
        contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] reviewbytes = msgExtEntity.getContents();
                String messageid = msgExtEntity.getMessage_id();

                HandleGroupRequestActivity.startActivity((Activity) context, reviewbytes, messageid);
                if (applyGroupBean.getState() == -1) {
                    txt3.setText("");
                    ParamManager.getInstance().updateGroupApply(groupApplyKey, applyGroupBean.getTips(), applyGroupBean.getSource(), 0, applyGroupBean.getMsgid());
                }
            }
        });
    }
}
