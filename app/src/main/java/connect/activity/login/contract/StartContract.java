package connect.activity.login.contract;

import android.app.Activity;

import connect.activity.base.BasePresenter;
import connect.activity.base.BaseView;

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
