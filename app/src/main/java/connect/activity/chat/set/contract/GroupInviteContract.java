package connect.activity.chat.set.contract;

import java.util.List;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.database.green.bean.ContactEntity;

/**
 * Created by Administrator on 2017/8/9.
 */

public interface GroupInviteContract {

    interface BView extends BaseView<GroupInviteContract.Presenter> {

        String getRoomKey();
    }

    interface Presenter extends BasePresenter {

        void requestGroupMemberInvite(List<ContactEntity> contactEntities);

    }
}
