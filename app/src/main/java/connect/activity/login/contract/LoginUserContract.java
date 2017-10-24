package connect.activity.login.contract;

import android.app.Activity;

import connect.activity.login.bean.UserBean;
import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

public interface LoginUserContract {

    interface View extends BaseView<LoginUserContract.Presenter> {
        Activity getActivity();

        void setNextBtnEnable(boolean isEnable);

        void launchHome();
    }

    interface Presenter extends BasePresenter {
        void passEditChange(String pass,String nick);

        void checkPassWord(String talkKey,String passWord,UserBean userBean);
    }

}
