package connect.activity.chat.set.contract;

import java.util.ArrayList;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.database.green.bean.ContactEntity;

/**
 * Created by Administrator on 2017/8/9.
 */

public interface GroupCreateSelectContract {

    interface BView extends BaseView<GroupCreateSelectContract.Presenter> {

        String getRoomKey();
    }

    interface Presenter extends BasePresenter {

        void requestGroupCreate(ArrayList<ContactEntity> contactEntities);

    }
}
