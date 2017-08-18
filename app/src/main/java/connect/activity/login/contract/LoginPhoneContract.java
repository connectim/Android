package connect.activity.login.contract;

import android.app.Activity;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

public interface LoginPhoneContract {

    interface View extends BaseView<LoginPhoneContract.Presenter> {
        void verifySuccess();

        void scanPermission();

        void goIntoRandomSend();

        void goIntoLocalLogin();

        Activity getActivity();
    }

    interface Presenter extends BasePresenter {
        void showMore();

        void request(String mobile);
    }
}
