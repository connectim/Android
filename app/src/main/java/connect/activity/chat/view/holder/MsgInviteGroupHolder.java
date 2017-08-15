package connect.activity.chat.view.holder;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import connect.activity.chat.bean.MsgExtEntity;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.activity.chat.bean.GroupExt1Bean;
import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.bean.MsgDirect;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.exts.ApplyJoinGroupActivity;
import connect.utils.glide.GlideUtil;
import connect.widget.roundedimageview.RoundedImageView;

/**
 * Created by pujin on 2017/1/21.
 */

public class MsgInviteGroupHolder extends MsgChatHolder {

    private RoundedImageView cardHead;
    private TextView txt1;
    private TextView txt2;

    private GroupExt1Bean ext1Bean;
    private int applyState = 0;

    public MsgInviteGroupHolder(View itemView) {
        super(itemView);
        cardHead = (RoundedImageView) itemView.findViewById(R.id.roundimg1);
        txt1 = (TextView) itemView.findViewById(R.id.txt1);
        txt2 = (TextView) itemView.findViewById(R.id.txt2);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final MsgExtEntity msgExtEntity) {
        super.buildRowData(msgBaseHolder, baseEntity);
        final MsgDefinBean definBean = baseEntity.getMsgDefinBean();
        ext1Bean = new Gson().fromJson(String.valueOf(definBean.getExt1()), GroupExt1Bean.class);

        GlideUtil.loadAvater(cardHead, ext1Bean.getAvatar());
        String showTxt = direct == MsgDirect.From ? context.getString(R.string.Link_Invite_you_to_join, ext1Bean.getGroupname()) :
                context.getString(R.string.Link_Invite_friend_to_join, ext1Bean.getGroupname());
        txt1.setText(showTxt);

        GroupExt1Bean reviewBean = new Gson().fromJson(definBean.getExt1(), GroupExt1Bean.class);
        applyState = ParamManager.getInstance().loadGroupApplyMember(reviewBean.getGroupidentifier(), definBean.getMessage_id());
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
                if (direct == MsgDirect.From) {
                    if (applyState == 0) {
                        ApplyJoinGroupActivity.startActivity((Activity) context, ApplyJoinGroupActivity.EApplyGroup.GROUPKEY, ext1Bean.getGroupidentifier(), baseEntity.getMsgDefinBean().getSenderInfoExt().address, ext1Bean.getInviteToken());
                    }
                }
            }
        });
    }
}
