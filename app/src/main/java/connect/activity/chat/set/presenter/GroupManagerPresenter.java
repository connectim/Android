package connect.activity.chat.set.presenter;

import android.app.Activity;
import android.text.TextUtils;

import connect.activity.chat.set.contract.GroupManagerContract;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupEntity;
import connect.utils.ActivityUtil;
import connect.utils.UriUtil;
import connect.utils.okhttp.OkHttpUtil;
import connect.utils.okhttp.ResultCall;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/9.
 */
public class GroupManagerPresenter implements GroupManagerContract.Presenter {

    private GroupManagerContract.BView view;
    private String roomKey;
    private Activity activity;

    public GroupManagerPresenter(GroupManagerContract.BView view) {
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

        boolean verify = (groupEntity.getVerify() != null) && (1 == groupEntity.getVerify());
        view.inviteSwitch(verify);

        view.groupIntroduce();
        view.groupNewOwner();
    }

    @Override
    public void requestGroupVerify(final boolean verify) {
        Connect.GroupSetting setting = Connect.GroupSetting.newBuilder()
                .setIdentifier(roomKey)
                .setPublic(verify).build();
        OkHttpUtil.getInstance().postEncrySelf(UriUtil.GROUP_SETTING, setting, new ResultCall<Connect.HttpResponse>() {

            @Override
            public void onResponse(Connect.HttpResponse response) {
                GroupEntity groupEntity = ContactHelper.getInstance().loadGroupEntity(roomKey);
                if (!(groupEntity == null || TextUtils.isEmpty(groupEntity.getEcdh_key()))) {
                    groupEntity.setVerify(verify ? 1 : 0);

                    String groupName = groupEntity.getName();
                    if (TextUtils.isEmpty(groupName)) {
                        groupName = "groupname6";
                    }
                    groupEntity.setName(groupName);
                    ContactHelper.getInstance().inserGroupEntity(groupEntity);
                }
            }

            @Override
            public void onError(Connect.HttpResponse response) {
                view.inviteSwitch(!verify);
            }
        });
    }
}
