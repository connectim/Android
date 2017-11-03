package connect.activity.set.contract;

import android.app.Activity;
import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

public interface SafetyLoginPassContract {

    interface View extends BaseView<SafetyLoginPassContract.Presenter> {
        Activity getActivity();

        void modifySuccess(int type);

        void changeBtnTiming(long time);

        void changeBtnFinish();
    }

    interface Presenter extends BasePresenter {
        void requestPassword(String password, String code, int type);

        void requestSendCode(String phone);
    }

}
