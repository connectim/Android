package connect.activity.login.contract;

import android.app.Activity;

import connect.activity.login.bean.UserBean;
import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/4/14 0014.
 */

public interface RegisterContract {

    interface View extends BaseView<RegisterContract.Presenter> {
        Activity getActivity();

        void setNextBtnEnable(boolean isEnable);

        void setPasswordhint(String text);

        void showAvatar(String path);

        void complete(boolean isBack);
    }

    interface Presenter extends BasePresenter {
        void editChange(String passWord,String nick);

        void requestUserHead(String localPaths);

        void registerUser(String nicname, String password,String token, UserBean userBean);

        void setPasswordHintData(String passwordHint);
    }

}
