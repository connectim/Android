package connect.activity.set.contract;

import android.app.Activity;
import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

public interface SafetyLoginPassContract {

    interface View extends BaseView<SafetyLoginPassContract.Presenter> {
        void setHint(String value);

        Activity getActivity();

        void modifySuccess();
    }

    interface Presenter extends BasePresenter {
        void requestPass(String pass,String hint);
    }

}
