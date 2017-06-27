package connect.activity.chat.view.holder;

import android.app.Activity;
import android.view.View;
import android.widget.TextView;

import com.google.gson.Gson;

import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.activity.chat.bean.ApplyGroupBean;
import connect.activity.chat.bean.MsgEntity;
import connect.activity.chat.bean.CardExt1Bean;
import connect.activity.chat.bean.GroupReviewBean;
import connect.activity.chat.bean.MsgDefinBean;
import connect.activity.chat.exts.HandleJoinGroupActivity;
import connect.utils.glide.GlideUtil;
import connect.widget.roundedimageview.RoundedImageView;

/**
 * Created by pujin on 2017/1/21.
 */

public class MsgHandleJoinGroupHolder extends MsgChatHolder {

    private RoundedImageView roundedImageView;
    private TextView txt1;
    private TextView txt3;

    private CardExt1Bean cardExt1Bean;
    private String groupApplyKey = null;
    private ApplyGroupBean applyGroupBean;

    public MsgHandleJoinGroupHolder(View itemView) {
        super(itemView);
        roundedImageView = (RoundedImageView) itemView.findViewById(R.id.roundimg1);
        txt1 = (TextView) itemView.findViewById(R.id.txt1);
        txt3= (TextView) itemView.findViewById(R.id.txt3);
    }

    @Override
    public void buildRowData(MsgBaseHolder msgBaseHolder, final MsgEntity baseEntity) {
        super.buildRowData(msgBaseHolder, baseEntity);
        final MsgDefinBean definBean = baseEntity.getMsgDefinBean();
        cardExt1Bean = new Gson().fromJson(String.valueOf(definBean.getExt1()), CardExt1Bean.class);

        GlideUtil.loadAvater(roundedImageView, cardExt1Bean.getAvatar());
        GroupReviewBean reviewBean = new Gson().fromJson(definBean.getContent(), GroupReviewBean.class);
        txt1.setText(context.getString(R.string.Link_apply_to_join_group_chat, cardExt1Bean.getUsername(), reviewBean.getGroupName()));

        groupApplyKey = reviewBean.getGroupKey() + cardExt1Bean.getPub_key();
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
                HandleJoinGroupActivity.startActivity((Activity) context, baseEntity);
                if (applyGroupBean.getState() == -1) {
                    txt3.setText("");
                    ParamManager.getInstance().updateGroupApply(groupApplyKey, applyGroupBean.getTips(), applyGroupBean.getSource(), 0, applyGroupBean.getMsgid());
                }
            }
        });
    }
}
