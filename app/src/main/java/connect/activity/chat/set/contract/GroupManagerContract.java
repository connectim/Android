package connect.activity.chat.set.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/8/9.
 */

public interface GroupManagerContract {

    interface BView extends BaseView<GroupManagerContract.Presenter> {

        String getRoomKey();

        void inviteSwitch(boolean avaliable);

        void groupIntroduce();

        void groupNewOwner();
    }

    interface Presenter extends BasePresenter {

        void requestGroupVerify(boolean verify);

    }
}
