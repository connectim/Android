package connect.activity.chat.set.contract;

import java.util.List;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import protos.Connect;

/**
 * Created by PuJin on 2018/1/11.
 */

public interface GroupCreateContract {

    interface BView extends BaseView<GroupCreateContract.Presenter> {

        List<Connect.Workmate> groupMemberList();

        void setLeftEnanle(boolean b);
    }

    interface Presenter extends BasePresenter {
        void createGroup(String groupName);
    }
}
