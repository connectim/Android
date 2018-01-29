package connect.activity.chat.set.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by PuJin on 2018/1/11.
 */

public interface BaseGroupSelectContract {

    interface BView extends BaseView<BaseGroupSelectContract.Presenter> {

        String getRoomKey();

    }

    interface Presenter extends BasePresenter {

    }
}
