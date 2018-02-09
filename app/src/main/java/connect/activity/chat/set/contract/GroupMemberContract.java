package connect.activity.chat.set.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/8/9.
 */

public interface GroupMemberContract {

    interface BView extends BaseView<GroupMemberContract.Presenter> {

        String getRoomKey();

    }

    interface Presenter extends BasePresenter {

    }
}
