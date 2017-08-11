package connect.activity.chat.exts.contract;

import java.util.List;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import protos.Connect;

/**
 * Created by Administrator on 2017/8/11.
 */

public interface GroupCrowdingRecordContract {

    interface BView extends BaseView<GroupCrowdingRecordContract.Presenter> {

        void crowdingRecords(List<Connect.Crowdfunding> list);
    }

    interface Presenter extends BasePresenter {

        void requestGroupCrowdingRecords(int page,int maxsize);
    }

}
