package connect.activity.chat.set.presenter;

import android.app.Activity;

import connect.activity.chat.set.contract.GroupMemberContract;

/**
 * Created by Administrator on 2017/8/9.
 */

public class GroupMemberPresenter implements GroupMemberContract.Presenter{

    private GroupMemberContract.BView view;
    private Activity activity;
    private String roomKey;

    public GroupMemberPresenter(GroupMemberContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }
}
