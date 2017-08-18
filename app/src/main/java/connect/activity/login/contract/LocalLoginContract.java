package connect.activity.login.contract;

import android.app.Activity;

import connect.activity.login.bean.UserBean;
import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

public interface LocalLoginContract {
    interface View extends BaseView<LocalLoginContract.Presenter> {
        Activity getActivity();

        void complete(boolean isBack);
    }

    interface Presenter extends BasePresenter {
        void checkTalkKey(String talkKey,String passWord,UserBean userBean);
    }

}
