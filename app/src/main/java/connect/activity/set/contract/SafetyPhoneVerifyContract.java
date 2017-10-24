package connect.activity.set.contract;

import android.app.Activity;

import connect.activity.base.contract.BasePresenter;
import connect.activity.base.contract.BaseView;

public interface SafetyPhoneVerifyContract {

    interface View extends BaseView<SafetyPhoneVerifyContract.Presenter> {
        Activity getActivity();

        String getCode();

        void changeBtnTiming(long time);

        void changeBtnFinish();
    }

    interface Presenter extends BasePresenter {
        void reSendCode(int type);

        void requestBindMobile(String type);

        void pauseDownTimer();
    }

}
