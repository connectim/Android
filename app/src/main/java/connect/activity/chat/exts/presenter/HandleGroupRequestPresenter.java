package connect.activity.chat.exts.presenter;

import android.app.Activity;
import android.text.TextUtils;

import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.Talker;
import connect.activity.chat.exts.contract.HandleGroupRequestContract;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.StringUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.cryption.EncryptionUtil;
import connect.utils.cryption.SupportKeyUril;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/11.
 */

public class HandleGroupRequestPresenter implements HandleGroupRequestContract.Presenter{

    private HandleGroupRequestContract.BView view;
    private Activity activity;
    private String groupKey;
    private GroupEntity groupEntity;

    public HandleGroupRequestPresenter(HandleGroupRequestContract.BView view){
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        activity = view.getActivity();
        groupKey = view.getPubKey();

        groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
        if (groupEntity == null) {
            ActivityUtil.goBack(activity);
            return;
        }

        requestGroupInfo();
    }

    @Override
    public void requestGroupInfo() {
        Connect.GroupId groupId = Connect.GroupId.newBuilder()
                .setIdentifier(groupKey)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_PUBLIC_INFO, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GroupInfoBase groupInfoBase = Connect.GroupInfoBase.parseFrom(structData.getPlainData());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(groupInfoBase)) {
                        String avatar = groupInfoBase.getAvatar();
                        String name = groupInfoBase.getName();
                        String summary = groupInfoBase.getSummary();
                        int member = groupInfoBase.getCount();

                        view.showGroupInfo(avatar, name, summary, member);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(activity, R.string.Link_Group_invitation_is_invalid,ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });
    }

    @Override
    public void agreeRequest(String caPublicKey,String code,String applyUid) {
        Connect.CreateGroupMessage createGroupMessage = Connect.CreateGroupMessage.newBuilder()
                .setIdentifier(groupKey)
                //qwert
                //.setSecretKey(groupEntity.getEcdh_key())
                .build();

        String myCaPrivateKey = SharedPreferenceUtil.getInstance().getUser().getPriKey();
        byte[] memberecdhkey = SupportKeyUril.getRawECDHKey(myCaPrivateKey, caPublicKey);
        Connect.GcmData gcmData = EncryptionUtil.encodeAESGCMStructData(EncryptionUtil.ExtendedECDH.EMPTY, memberecdhkey, createGroupMessage.toByteString());

        String myCaPublicKey = SharedPreferenceUtil.getInstance().getUser().getPubKey();
        String groupHex = StringUtil.bytesToHexString(gcmData.toByteArray());
        String backup = String.format("%1$s/%2$s", myCaPublicKey, groupHex);

        Connect.GroupReviewed reviewed = Connect.GroupReviewed.newBuilder()
                .setIdentifier(groupKey)
                .setUid(applyUid)
                .setVerificationCode(code)
                .setBackup(backup)
                .build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_REVIEWED, reviewed, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
                if (groupEntity != null) {
                    view.updateGroupRequest(1);
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                switch (response.getCode()) {
                    case 2432://not group manager
                        ToastEUtil.makeText(activity, activity.getString(R.string.Chat_Not_Group_Master), 2).show();
                        break;
                    case 2433://be in group
                        ToastEUtil.makeText(activity, activity.getString(R.string.Chat_User_already_in_group), 2).show();
                        break;
                    case 3434://Verification code error
                        ToastEUtil.makeText(activity, activity.getString(R.string.Chat_VerifyCode_has_expired), 2).show();
                        break;
                }
            }
        });
    }

    @Override
    public void rejectRequest(String code,String applyUid) {
        Connect.GroupReviewed reviewed = Connect.GroupReviewed.newBuilder()
                .setIdentifier(groupKey)
                .setVerificationCode(code)
                .setUid(applyUid)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_REJECT, reviewed, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                view.updateGroupRequest(2);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if (response.getCode() == 2425) {
                    ToastEUtil.makeText(activity, R.string.Chat_VerifyCode_has_expired, ToastEUtil.TOAST_STATUS_FAILE).show();
                } else {
                    String errorMessage = response.getMessage();
                    if (TextUtils.isEmpty(errorMessage)) {
                        errorMessage = activity.getString(R.string.Network_equest_failed_please_try_again_later);
                    }
                    ToastEUtil.makeText(activity, errorMessage, ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        });
    }

    @Override
    public void groupChat() {
        if (groupEntity != null) {
            ChatActivity.startActivity(activity, new Talker(Connect.ChatType.GROUPCHAT,groupEntity.getIdentifier()));
        }
    }
}
