package connect.activity.login.contract;

import android.app.Activity;
import android.text.TextWatcher;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/4/14 0014.
 */

/**
 * Created by pujin on 17-4-28.
 */

public interface LoginPhoneContract {

    interface View extends BaseView<LoginPhoneContract.Presenter> {
        void setBtnEnabled(boolean isEnabled);

        void verifySuccess();

        void scanPermission();

        void goinRandomSend();

        void goinLocalLogin();

        Activity getActivity();
    }

    interface Presenter extends BasePresenter {
        void showMore();

        void request(String mobile);

        TextWatcher getPhoneTextWatcher();
    }
}
