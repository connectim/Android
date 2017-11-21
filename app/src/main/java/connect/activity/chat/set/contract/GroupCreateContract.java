package connect.activity.chat.set.contract;

import java.util.List;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.database.green.bean.ContactEntity;

/**
 * Created by Administrator on 2017/11/20.
 */

public interface GroupCreateContract {

    interface BView extends BaseView<GroupCreateContract.Presenter> {
        List<ContactEntity> groupMemberList();
    }

    interface Presenter extends BasePresenter {
        void createGroup(String groupName,int groupCategory);
    }
}
