package connect.activity.login.contract;

import android.app.Activity;
import android.os.Handler;

import connect.activity.login.bean.UserBean;
import connect.activity.base.BasePresenter;
import connect.activity.base.BaseView;

/**
 * Created by Administrator on 2017/4/14 0014.
 */

public interface ScanLoginContract {

    interface View extends BaseView<ScanLoginContract.Presenter> {
        Activity getActivity();

        void goinCodeLogin(UserBean userBean,String token);

        void goinRegister(String priKey);
    }

    interface Presenter extends BasePresenter {
        Handler getHandle();

        void checkString(String value);
    }

}
