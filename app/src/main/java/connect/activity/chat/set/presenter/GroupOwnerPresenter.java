package connect.activity.chat.set.presenter;

import android.app.Activity;
import android.text.TextUtils;

import connect.activity.chat.set.GroupSetActivity;
import connect.activity.chat.set.contract.GroupOwnerContract;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.ui.activity.R;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/8.
 */

public class GroupOwnerPresenter implements GroupOwnerContract.Presenter{

    private GroupOwnerContract.BView view;

    private String roomKey;
    private Activity activity;

    public GroupOwnerPresenter(GroupOwnerContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        roomKey = view.getRoomKey();
        activity = view.getActivity();
    }

    @Override
    public void groupOwnerTo(String memberKey, final String memberUid) {
        Connect.GroupAttorn attorn = Connect.GroupAttorn.newBuilder()
                .setIdentifier(memberKey)
                .setUid(memberUid)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_ATTORN, attorn, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                String myUid = SharedPreferenceUtil.getInstance().getUser().getUid();
                ContactHelper.getInstance().updateGroupMemberRole(roomKey, myUid, 0);
                ContactHelper.getInstance().updateGroupMemberRole(roomKey, memberUid, 1);

                GroupSetActivity.startActivity(activity, roomKey);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                String message = response.getMessage();
                if (TextUtils.isEmpty(message)) {
                    message = activity.getString(R.string.Network_equest_failed_please_try_again_later);
                }
                ToastEUtil.makeText(activity, message, ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });
    }
}
