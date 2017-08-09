package connect.activity.chat.set.contract;

import java.util.List;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.database.green.bean.ContactEntity;

/**
 * Created by Administrator on 2017/8/9.
 */

public interface GroupCreateContract {

    interface BView extends BaseView<GroupCreateContract.Presenter> {

        String getRoomKey();
    }

    interface Presenter extends BasePresenter {

        void requestGroupCreate(List<ContactEntity> contactEntities);

    }
}
