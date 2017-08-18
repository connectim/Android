package connect.activity.login.contract;

import android.app.Activity;

import connect.activity.login.bean.UserBean;
import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

public interface RegisterContract {

    interface View extends BaseView<RegisterContract.Presenter> {
        Activity getActivity();

        void setPasswordhint(String text);

        void showAvatar(String path);

        void complete(boolean isBack);
    }

    interface Presenter extends BasePresenter {
        void requestUserHead(String localPaths);

        void registerUser(String nicname, String password,String token, UserBean userBean);

        void setPasswordHintData(String passwordHint);
    }

}
