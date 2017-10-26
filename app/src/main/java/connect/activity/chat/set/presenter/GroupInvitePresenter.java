package connect.activity.chat.set.presenter;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import connect.activity.chat.set.contract.GroupInviteContract;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.DaoHelper.MessageHelper;
import connect.database.green.bean.ContactEntity;
import connect.database.green.bean.GroupEntity;
import connect.instant.model.CFriendChat;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import instant.bean.ChatMsgEntity;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/9.
 */

public class GroupInvitePresenter implements GroupInviteContract.Presenter{

    private GroupInviteContract.BView view;
    private String groupKey;
    private Activity activity;

    public GroupInvitePresenter(GroupInviteContract.BView view){
        this.view=view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        this.groupKey = view.getRoomKey();
        this.activity = view.getActivity();
    }

    @Override
    public void requestGroupMemberInvite(List<ContactEntity> contactEntities) {
        Connect.GroupInviteUser.Builder builder = Connect.GroupInviteUser.newBuilder();
        builder.setIdentifier(groupKey);

        List<String> addStrs = new ArrayList<>();
        for (ContactEntity contactEntity : contactEntities) {
            addStrs.add(contactEntity.getUid());
        }
        builder.addAllUids(addStrs);
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_INVITE_TOKEN, builder.build(), new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    if (!SupportKeyUril.verifySign(imResponse.getSign(), imResponse.getCipherData().toByteArray())) {
                        throw new Exception("Validation fails");
                    }

                    GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);

                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GroupInviteResponseList responseList = Connect.GroupInviteResponseList.parseFrom(structData.getPlainData());
                    for (Connect.GroupInviteResponse res : responseList.getListList()) {
                        if (ProtoBufUtil.getInstance().checkProtoBuf(res)) {
                            String uid = res.getUid();
                            String token = res.getToken();

                            ContactEntity friendEntity = ContactHelper.getInstance().loadFriendEntity(uid);
                            if (friendEntity != null) {
                                CFriendChat friendChat = new CFriendChat(friendEntity);
                                ChatMsgEntity msgExtEntity = friendChat.inviteJoinGroupMsg(groupEntity.getAvatar(), groupEntity.getName(),
                                        groupEntity.getIdentifier(), token);

                                friendChat.sendPushMsg(msgExtEntity);
                                MessageHelper.getInstance().insertMsgExtEntity(msgExtEntity);
                                friendChat.updateRoomMsg(null, "[" + activity.getString(R.string.Link_Join_Group) + "]", msgExtEntity.getCreatetime());
                            }
                        }
                    }

                    ToastEUtil.makeText(activity, activity.getString(R.string.Link_Send_successful), 1, new ToastEUtil.OnToastListener() {

                        @Override
                        public void animFinish() {
                            ActivityUtil.goBack(activity);
                        }
                    }).show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(activity, activity.getString(R.string.Network_equest_failed_please_try_again_later), 2).show();
            }
        });
    }
}
