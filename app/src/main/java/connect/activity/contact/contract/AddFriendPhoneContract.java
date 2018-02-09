package connect.activity.contact.contract;

import android.app.Activity;

import java.util.List;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;
import connect.activity.contact.bean.PhoneContactBean;

public interface AddFriendPhoneContract {

    interface View extends BaseView<AddFriendPhoneContract.Presenter> {
        Activity getActivity();

        void updateView(int size, List<PhoneContactBean> list);
    }

    interface Presenter extends BasePresenter {
        void requestContact();

        void sendMessage(List<PhoneContactBean> list);
    }

}
