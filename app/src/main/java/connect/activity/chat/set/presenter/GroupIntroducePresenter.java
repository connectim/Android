package connect.activity.chat.set.presenter;

import android.app.Activity;
import android.text.TextUtils;

import connect.activity.chat.set.GroupManageActivity;
import connect.activity.chat.set.contract.GroupIntroduceContract;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupEntity;
import connect.utils.ActivityUtil;
import connect.utils.ToastEUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/9.
 */

public class GroupIntroducePresenter implements GroupIntroduceContract.Presenter{

    private GroupIntroduceContract.BView view;
    private String roomKey;
    private Activity activity;

    public GroupIntroducePresenter(GroupIntroduceContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {
        roomKey = view.getRoomKey();
        activity = view.getActivity();

        GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(roomKey);
        if (groupEntity == null) {
            ActivityUtil.goBack(activity);
            return;
        }

        String groupintroduce = groupEntity.getSummary();
        if (TextUtils.isEmpty(groupintroduce)) {
            groupintroduce = groupEntity.getName();
        }
        view.groupIntroduce(groupintroduce);
    }

    @Override
    public void requestUpdateGroupSummary(final String introduce) {
        Connect.GroupSetting setting = Connect.GroupSetting.newBuilder()
                .setIdentifier(roomKey)
                .setSummary(introduce)
                .setPublic(true).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_SETTING, setting, new ResultCall<Connect.HttpResponse>() {
            @Override
            public void onResponse(Connect.HttpResponse response) {
                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(roomKey);
                if (!(groupEntity == null || TextUtils.isEmpty(groupEntity.getName()) || TextUtils.isEmpty(groupEntity.getEcdh_key()))) {
                    groupEntity.setSummary(introduce);
                    ContactHelper.getInstance().inserGroupEntity(groupEntity);
                }
                GroupManageActivity.startActivity(activity, roomKey);
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                String message = response.getMessage();
                if (!TextUtils.isEmpty(message)) {
                    ToastEUtil.makeText(activity, response.getMessage(), ToastEUtil.TOAST_STATUS_FAILE).show();
                }
            }
        });
    }
}
