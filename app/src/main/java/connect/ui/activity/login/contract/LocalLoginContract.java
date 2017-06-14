package connect.ui.activity.login.contract;

import android.app.Activity;

import connect.ui.activity.login.bean.UserBean;
import connect.ui.base.BasePresenter;
import connect.ui.base.BaseView;

/**
 * Created by Administrator on 2017/4/14 0014.
 */

public interface LocalLoginContract {
    interface View extends BaseView<LocalLoginContract.Presenter> {
        Activity getActivity();

        void complete(boolean isBack);
    }

    interface Presenter extends BasePresenter {
        void checkTalkKey(String talkKey,String passWord,UserBean userBean);

    }

}
