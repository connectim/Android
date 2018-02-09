package connect.activity.chat.exts.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/8/11.
 */

public interface GroupAtContract {

    interface BView extends BaseView<GroupAtContract.Presenter> {

        String getGroupKey();
    }

    interface Presenter extends BasePresenter {

    }
}
