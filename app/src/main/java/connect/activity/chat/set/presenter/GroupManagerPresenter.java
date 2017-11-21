package connect.activity.chat.set.presenter;

import android.app.Activity;

import connect.activity.chat.set.contract.GroupManagerContract;
import connect.database.green.DaoHelper.ContactHelper;
import connect.database.green.bean.GroupEntity;
import connect.utils.ActivityUtil;

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

        view.groupIntroduce();
        view.groupNewOwner();
    }
}
