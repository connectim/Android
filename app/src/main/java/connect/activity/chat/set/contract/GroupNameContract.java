package connect.activity.chat.set.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/8/8.
 */

public interface GroupNameContract {

    interface BView extends BaseView<GroupNameContract.Presenter> {

        String getRoomKey();

        void groupName(String groupname);
    }

    interface Presenter extends BasePresenter {

        void updateGroupName(String groupname);

    }
}
