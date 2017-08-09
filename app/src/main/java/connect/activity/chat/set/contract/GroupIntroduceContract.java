package connect.activity.chat.set.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/8/9.
 */

public interface GroupIntroduceContract {

    interface BView extends BaseView<GroupIntroduceContract.Presenter> {

        String getRoomKey();

        void groupIntroduce(String introduce);
    }

    interface Presenter extends BasePresenter {

        void requestUpdateGroupSummary(String introduce);

    }
}
