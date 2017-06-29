package connect.activity.login.contract;

import android.app.Activity;

import connect.activity.login.bean.UserBean;
import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/4/14 0014.
 */

public interface CodeLoginContract {

    interface View extends BaseView<CodeLoginContract.Presenter> {
        Activity getActivity();

        void setNextBtnEnable(boolean isEnable);

        void setPasswordhint(String text);

        void goinHome(boolean isBack);
    }

    interface Presenter extends BasePresenter {
        void passEditChange(String pass,String nick);

        void checkTalkKey(String talkKey,String passWord,UserBean userBean);

        void requestSetPassword(String password,UserBean userBean,String token);

        void setPasswordHintData(String passwordHint);
    }

}
