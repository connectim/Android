package connect.activity.chat.exts.presenter;

import android.app.Activity;
import android.text.TextUtils;

import connect.activity.chat.ChatActivity;
import connect.activity.chat.bean.Talker;
import connect.activity.chat.exts.contract.JoinGroupContract;
import connect.activity.home.bean.GroupRecBean;
import connect.ui.activity.R;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by puin on 17-8-11.
 */

public class JoinGroupPresenter implements JoinGroupContract.Presenter{

    private JoinGroupContract.BView view;
    private Activity activity;

    public JoinGroupPresenter(JoinGroupContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        activity = view.getActivity();
    }

    @Override
    public void requestByToken(String token) {
        Connect.GroupToken groupToken = Connect.GroupToken.newBuilder()
                .setToken(token)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_GROUP_INFOTOKEN, groupToken, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = Connect.StructData.parseFrom(imResponse.getBody());
                    Connect.GroupInfoBaseShare baseShare = Connect.GroupInfoBaseShare.parseFrom(structData.getPlainData());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(baseShare)){
                        String groupkey = baseShare.getIdentifier();
                        Connect.GroupInfoBase infoBase = Connect.GroupInfoBase.newBuilder()
                                .setAvatar(baseShare.getAvatar())
                                .setHash(baseShare.getHash())
                                .setCount(baseShare.getCount())
                                .setName(baseShare.getName())
                                .setPublic(baseShare.getPublic())
                                .setJoined(baseShare.getJoined())
                                .setSummary(baseShare.getSummary()).build();

                        view.showTokenInfo(groupkey, infoBase);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                view.showFailInfo();
                String contentTxt = response.getMessage();
                if (TextUtils.isEmpty(contentTxt)) {
                    ToastEUtil.makeText(activity, activity.getString(R.string.Network_equest_failed_please_try_again_later), 2).show();
                } else {
                    ToastEUtil.makeText(activity, contentTxt, 2).show();
                }
            }
        });
    }

    @Override
    public void requestByGroupkey(String groupkey) {
        Connect.GroupId groupId = Connect.GroupId.newBuilder()
                .setIdentifier(groupkey)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_PUBLIC_INFO, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = Connect.StructData.parseFrom(imResponse.getBody());
                    Connect.GroupInfoBase groupInfoBase = Connect.GroupInfoBase.parseFrom(structData.getPlainData());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(groupInfoBase)){
                        view.showGroupkeyInfo(groupInfoBase);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                view.showFailInfo();
                String contentTxt = response.getMessage();
                if (TextUtils.isEmpty(contentTxt)) {
                    ToastEUtil.makeText(activity, activity.getString(R.string.Network_equest_failed_please_try_again_later), 2).show();
                } else {
                    ToastEUtil.makeText(activity, contentTxt, 2).show();
                }
            }
        });
    }

    @Override
    public void requestByLink(String groupkey, String hash) {
        Connect.GroupScan groupId = Connect.GroupScan.newBuilder()
                .setIdentifier(groupkey)
                .setHash(hash)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_PUBLIC_INFO, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = Connect.StructData.parseFrom(imResponse.getBody());
                    Connect.GroupInfoBase groupInfoBase = Connect.GroupInfoBase.parseFrom(structData.getPlainData());
                    if (ProtoBufUtil.getInstance().checkProtoBuf(groupInfoBase)) {
                        view.showLinkInfo(groupInfoBase);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                view.showFailInfo();
                String contentTxt = response.getMessage();
                if (TextUtils.isEmpty(contentTxt)) {
                    ToastEUtil.makeText(activity, activity.getString(R.string.Network_equest_failed_please_try_again_later), 2).show();
                } else {
                    ToastEUtil.makeText(activity, contentTxt, 2).show();
                }
            }
        });
    }

    @Override
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
                ToastEUtil.makeText(activity, activity.getString(R.string.Link_Send_successful), 1, new ToastEUtil.OnToastListener() {
                    @Override
                    public void animFinish() {
                        ChatActivity.startActivity(activity, new Talker(Connect.ChatType.GROUP_DISCUSSION, groupkey));
                    }
                }).show();
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if(response.getCode() == 2430){
                    ToastEUtil.makeText(activity, R.string.Link_Qr_code_is_invalid,ToastEUtil.TOAST_STATUS_FAILE).show();
                }else{
                    String contentTxt = response.getMessage();
                    if (TextUtils.isEmpty(contentTxt)) {
                        ToastEUtil.makeText(activity, activity.getString(R.string.Network_equest_failed_please_try_again_later), 2).show();
                    } else {
                        ToastEUtil.makeText(activity, contentTxt, 2).show();
                    }
                }
            }
        });
    }

    @Override
    public void requestJoinByLink(final String groupkey, String hash, String tips, int source) {
        Connect.GroupApply apply = Connect.GroupApply.newBuilder()
                .setIdentifier(groupkey)
                .setHash(hash)
                .setTips(tips)
                .setSource(source)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_APPLY, apply, new ResultCall<Connect.HttpResponse>() {

            @Override
            public void onResponse(Connect.HttpResponse response) {
                GroupRecBean.sendGroupRecMsg(GroupRecBean.GroupRecType.GroupInfo, groupkey);
                ToastEUtil.makeText(activity, activity.getString(R.string.Link_Send_successful), 1, new ToastEUtil.OnToastListener() {
                    @Override
                    public void animFinish() {
                        ChatActivity.startActivity(activity, new Talker(Connect.ChatType.GROUP_DISCUSSION, groupkey));
                    }
                }).show();
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                if (response.getCode() == 2403) {
                    ToastEUtil.makeText(activity, R.string.Link_have_joined_the_group, ToastEUtil.TOAST_STATUS_FAILE).show();
                }else{
                    String contentTxt = response.getMessage();
                    if (TextUtils.isEmpty(contentTxt)) {
                        ToastEUtil.makeText(activity, activity.getString(R.string.Network_equest_failed_please_try_again_later), 2).show();
                    } else {
                        ToastEUtil.makeText(activity, contentTxt, 2).show();
                    }
                }
            }
        });
    }
}
