package connect.activity.chat.view.holder;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.Talker;
import connect.activity.home.bean.GroupRecBean;
import connect.database.green.DaoHelper.ParamManager;
import connect.ui.activity.R;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.glide.GlideUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import instant.bean.ChatMsgEntity;
import instant.bean.MsgDirect;
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
                        String groupId = joinGroupMessage.getGroupId();
                        String messageIdentify = getMsgExtEntity().getMessage_ower();
                        String token = joinGroupMessage.getToken();

                        requestJoinByInvite(groupId, messageIdentify, "", token);
                    }
                }
            }
        });
    }


    public void requestJoinByInvite(final String groupkey, String inviteby, String tips, String token) {
        Connect.GroupInvite invite = Connect.GroupInvite.newBuilder()
                .setIdentifier(groupkey)
                .setInviteBy(inviteby)
                .setTips(tips)
                .setToken(token)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_INVITE, invite, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.GroupInfo, groupkey);
                ToastEUtil.makeText((Activity) context, ((Activity) context).getString(R.string.Link_Join_Group_Success), 1, new ToastEUtil.OnToastListener() {
                    @Override
                    public void animFinish() {
                        ChatActivity.startActivity((Activity) context, new Talker(Connect.ChatType.GROUP_DISCUSSION, groupkey));
                    }
                }).show();
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if (response.getCode() == 2430) {
                    ToastEUtil.makeText((Activity) context, R.string.Link_Qr_code_is_invalid, ToastEUtil.TOAST_STATUS_FAILE).show();
                } else if (response.getCode() == 2403) {
                    ToastEUtil.makeText((Activity) context, ((Activity) context).getString(R.string.Link_Join_Group_Success), 1, new ToastEUtil.OnToastListener() {
                        @Override
                        public void animFinish() {
                            ChatActivity.startActivity((Activity) context, new Talker(Connect.ChatType.GROUP_DISCUSSION, groupkey));
                        }
                    }).show();
                } else {
                    String contentTxt = response.getMessage();
                    if (TextUtils.isEmpty(contentTxt)) {
                        ToastEUtil.makeText((Activity) context, ((Activity) context).getString(R.string.Network_equest_failed_please_try_again_later), 2).show();
                    } else {
                        ToastEUtil.makeText((Activity) context, contentTxt, 2).show();
                    }
                }
            }
        });
    }
}
