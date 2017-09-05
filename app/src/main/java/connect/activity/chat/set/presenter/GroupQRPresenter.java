package connect.activity.chat.set.presenter;

import android.app.Activity;
import android.content.Intent;

import connect.activity.chat.set.contract.GroupQRContract;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ProtoBufUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.cryption.DecryptionUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/8.
 */
public class GroupQRPresenter implements GroupQRContract.Presenter {

    private GroupQRContract.BView view;

    private String roomKey;
    private Activity activity;

    public GroupQRPresenter(GroupQRContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        roomKey = view.getRoomKey();
        activity=view.getActivity();

        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(roomKey);
        if (groupEntity == null) {
            ActivityUtil.goBack(activity);
            return;
        }

        view.groupAvatar(groupEntity.getAvatar());
    }

    @Override
    public void requestGroupQR(String url) {
        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(roomKey);
        Connect.GroupId groupId = Connect.GroupId.newBuilder()
                .setIdentifier(groupEntity.getIdentifier()).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_HASH, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GroupHash groupHash = Connect.GroupHash.parseFrom(structData.getPlainData());
                    if(ProtoBufUtil.getInstance().checkProtoBuf(groupHash)){
                        String hash = groupHash.getHash();
                        view.groupHash(hash);
                    }
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

    @Override
    public void requestGroupShare() {
        Connect.GroupId groupId= Connect.GroupId.newBuilder()
                .setIdentifier(roomKey).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.CONNECT_GROUP_SHARE, groupId, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                try {
                    Connect.IMResponse imResponse = Connect.IMResponse.parseFrom(response.getBody().toByteArray());
                    Connect.StructData structData = DecryptionUtil.decodeAESGCMStructData(imResponse.getCipherData());
                    Connect.GroupUrl groupUrl = Connect.GroupUrl.parseFrom(structData.getPlainData());
                    if(!ProtoBufUtil.getInstance().checkProtoBuf(groupUrl)){
                        ToastEUtil.makeText(activity,R.string.Link_Share_failed,ToastEUtil.TOAST_STATUS_FAILE).show();
                        return;
                    }
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, groupUrl.getUrl());
                    shareIntent.setType("text/plain");
                    activity.startActivity(Intent.createChooser(shareIntent, "share to"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(activity,R.string.Link_The_group_is_not_public_Not_Share,ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });
    }
}
