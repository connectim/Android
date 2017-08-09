package connect.activity.login.contract;

import android.app.Activity;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

/**
 * Created by Administrator on 2017/4/14 0014.
 */

public interface StartContract {

    interface View extends BaseView<Presenter> {
        void setImage(String path);

        void goinGuide();

        void goinLoginForPhone();

        void goinLoginPatter();

        void goinHome();

        Activity getActivity();
    }

    interface Presenter extends BasePresenter {

    }

}
