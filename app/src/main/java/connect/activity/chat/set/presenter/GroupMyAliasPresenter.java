package connect.activity.chat.set.presenter;

import android.app.Activity;
import android.text.TextUtils;

import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.set.GroupSetActivity;
import connect.activity.chat.set.contract.GroupMyAliasContract;
import connect.database.SharedPreferenceUtil;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupMemberEntity;
import connect.ui.activity.R;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/8.
 */

public class GroupMyAliasPresenter implements GroupMyAliasContract.Presenter {

    private String groupKey;
    private Activity activity;
    private GroupMyAliasContract.BView view;

    public GroupMyAliasPresenter(GroupMyAliasContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        groupKey = view.getRoomKey();
        activity = view.getActivity();
        String myUid = SharedPreferenceUtil.getInstance().getUser().getUid();
        GroupMemberEntity myMemberEntity = ContactHelper.getInstance().loadGroupMemberEntity(groupKey, myUid);
        if (null != myMemberEntity) {
            String myGroupName= myMemberEntity.getUsername();
            view.myNameInGroup(myGroupName);
        }
    }

    @Override
    public void updateMyAliasInGroup(final String myalias) {
        Connect.UpdateGroupMemberInfo memberInfo = Connect.UpdateGroupMemberInfo.newBuilder()
                .setNick(myalias).setIdentifier(groupKey).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_MEMUPDATE, memberInfo, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                String myUid = SharedPreferenceUtil.getInstance().getUser().getUid();
                ContactHelper.getInstance().updateGroupMemberNickName(groupKey, myUid, myalias);

                RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.GROUP_UPDATEMYNAME);
                GroupSetActivity.startActivity(activity, groupKey);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                ToastEUtil.makeText(activity, R.string.Link_An_error_occurred_change_nickname, ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });
    }
}
