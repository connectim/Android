package connect.activity.chat.set.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/8/8.
 */

public interface GroupMyAliasContract {

    interface BView extends BaseView<GroupMyAliasContract.Presenter> {

        String getRoomKey();

        void myNameInGroup(String myalias);
    }

    interface Presenter extends BasePresenter {

        void updateMyAliasInGroup(String myalias);

    }
}
