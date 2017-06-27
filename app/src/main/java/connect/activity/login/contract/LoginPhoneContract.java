package connect.activity.login.contract;

import android.app.Activity;

import connect.activity.base.BasePresenter;
import connect.activity.base.BaseView;

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
    }
}
