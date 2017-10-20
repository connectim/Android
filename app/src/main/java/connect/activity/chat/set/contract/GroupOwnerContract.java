package connect.activity.chat.set.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/8/8.
 */

public interface GroupOwnerContract {

    interface BView extends BaseView<GroupOwnerContract.Presenter> {

        String getRoomKey();
    }

    interface Presenter extends BasePresenter {

        void groupOwnerTo(String memberKey,String uid);

    }
}
