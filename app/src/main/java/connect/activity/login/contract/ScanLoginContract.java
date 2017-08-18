package connect.activity.login.contract;

import android.app.Activity;
import android.os.Handler;

import connect.activity.login.bean.UserBean;
import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

public interface ScanLoginContract {

    interface View extends BaseView<ScanLoginContract.Presenter> {
        Activity getActivity();

        void goIntoCodeLogin(UserBean userBean,String token);

        void goIntoRegister(String priKey);
    }

    interface Presenter extends BasePresenter {
        void checkString(String value);
    }

}
