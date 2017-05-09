package connect.ui.activity.login.contract;

import android.app.Activity;

import connect.ui.activity.login.bean.UserBean;
import connect.ui.base.BasePresenter;
import connect.ui.base.BaseView;

/**
 * Created by Administrator on 2017/4/14 0014.
 */

public interface StartContract {

    interface View extends BaseView<Presenter> {
        void setImage(String path);

        void goinGuide();

        void goinLoginForPhone();

        void goinLoginPatter(UserBean userB);

        void goinHome();

        Activity getActivity();
    }

    interface Presenter extends BasePresenter {

    }

}
