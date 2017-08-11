package connect.activity.chat.exts.presenter;

import connect.activity.chat.exts.contract.GroupAtContract;

/**
 * Created by Administrator on 2017/8/11.
 */

public class GroupAtPresenter implements GroupAtContract.Presenter {

    private GroupAtContract.BView view;

    public GroupAtPresenter(GroupAtContract.BView view) {
        this.view = view;
        view.setPresenter(this);
    }

    @Override
    public void start() {

    }
}
