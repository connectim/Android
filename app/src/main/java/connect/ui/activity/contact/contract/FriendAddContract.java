package connect.ui.activity.contact.contract;

import android.app.Activity;

import java.util.List;

import connect.ui.base.BasePresenter;
import connect.ui.base.BaseView;
import connect.ui.activity.contact.bean.PhoneContactBean;

/**
 * Created by Administrator on 2017/4/19 0019.
 */

public interface FriendAddContract {

    interface View extends BaseView<FriendAddContract.Presenter> {
        Activity getActivity();

        void updataView(int size, List<PhoneContactBean> list);
    }

    interface Presenter extends BasePresenter {
        void requestContact();
    }

}
