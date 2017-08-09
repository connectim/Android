package connect.activity.chat.set.contract;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/8/8.
 */

public interface GroupQRContract {

    interface BView extends BaseView<GroupQRContract.Presenter> {

        String getRoomKey();

        void groupAvatar(String avatar);

        void groupName(String groupname);

        void groupHash(String hash);
    }

    interface Presenter extends BasePresenter {

        void requestGroupQR(String url);

        void requestGroupShare();
    }
}
