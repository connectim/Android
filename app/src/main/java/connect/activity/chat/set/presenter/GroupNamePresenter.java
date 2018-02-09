package connect.activity.chat.set.presenter;

import android.app.Activity;
import android.text.TextUtils;

import connect.activity.chat.bean.RecExtBean;
import connect.activity.chat.set.GroupSetActivity;
import connect.activity.chat.set.contract.GroupNameContract;
import connect.activity.contact.bean.ContactNotice;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupEntity;
import connect.ui.activity.R;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/8.
 */
public class GroupNamePresenter implements GroupNameContract.Presenter{

    private String groupKey;
    private Activity activity;
    private GroupNameContract.BView view;

    public GroupNamePresenter(GroupNameContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        groupKey = view.getRoomKey();
        activity = view.getActivity();

        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
        if (groupEntity == null) {
            ActivityUtil.goBack(activity);
            return;
        }
        view.groupName(groupEntity.getName());
    }

    @Override
    public void updateGroupName(final String groupName) {
        Connect.UpdateGroupInfo groupInfo = Connect.UpdateGroupInfo.newBuilder()
                .setIdentifier(groupKey)
                .setName(groupName)
                .build();

        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_UPDATE, groupInfo, new ResultCall<Connect.HttpResponse>() {

            @Override
            public void onResponse(Connect.HttpResponse response) {
                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(groupKey);
                if (!(groupEntity == null || TextUtils.isEmpty(groupName))) {
                    groupEntity.setName(groupName);
                    ContactHelper.getInstance().inserGroupEntity(groupEntity);

                    ContactNotice.receiverGroup();
                    RecExtBean.getInstance().sendEvent(RecExtBean.ExtType.UPDATENAME, groupKey,groupName);
                }
                GroupSetActivity.startActivity(activity,groupKey);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                String errorMessage = response.getMessage();
                if (TextUtils.isEmpty(errorMessage)) {
                    errorMessage = activity.getString(R.string.Link_Update_Group_Name_Failed);
                }
                ToastEUtil.makeText(activity, errorMessage, ToastEUtil.TOAST_STATUS_FAILE).show();
            }
        });
    }
}
